package com.willam.chatnotes

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ChatStoreTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var store: ChatStore
    private lateinit var name: String
    @Before fun setup() { name = "test-${UUID.randomUUID()}.db"; store = ChatStore(context, name) }
    @After fun teardown() { store.close(); context.deleteDatabase(name) }
    private fun put(cid: String, id: String, parent: String, role: String, text: String, status: String = "complete", source: String = "network") {
        store.applyEvent(JSONObject().put("type", "message").put("conversationId", cid).put("msgId", id)
            .put("parentId", parent).put("role", role).put("text", text).put("status", status)
            .put("source", source).put("createdAt", 100).put("select", true))
    }
    private fun seed(cid: String = "c") { put(cid,"u","","user","问题"); put(cid,"a","u","assistant","答复") }
    @Test fun sessionsAndBranchesAreIsolated() {
        seed("c1");seed("c2");put("c1","a2","u","assistant","重新生成")
        assertEquals(listOf("u","a2"),store.snapshot("c1").messages.map { it.id })
        assertEquals("答复",store.snapshot("c2").messages.last().text)
        assertEquals(3,store.allMessages("c1").size)
    }
    @Test fun repeatedCaptureDoesNotDuplicateAndNewMessagesCreateNewJobs() {
        seed();val old=store.createJob(store.snapshot("c"));seed()
        assertEquals(2,store.count("c"));assertEquals(old.id,store.createJob(store.snapshot("c")).id)
        put("c","u2","a","user","追问");put("c","a2","u2","assistant","新回复")
        assertNotEquals(old.id,store.createJob(store.snapshot("c")).id)
    }
    @Test fun partialMessageSurvivesClosingDatabase() {
        seed();put("c","a","u","assistant","生成了一半","streaming")
        store.close();store=ChatStore(context,name);store.recoverInterruptedCapture()
        assertEquals("生成了一半",store.snapshot("c").messages.last().text)
        assertEquals("partial",store.snapshot("c").messages.last().status)
    }
    @Test fun provisionalConversationMovesAtomically() {
        seed("local:abc")
        store.applyEvent(JSONObject().put("type","remap").put("from","local:abc").put("to","server"))
        assertEquals("server",store.resolve("local:abc"));assertEquals(2,store.count("server"))
        assertEquals(1,store.conversations().size)
    }
    @Test fun inFlightRequestsBlockPrematureSummary() {
        seed()
        fun event(state: String) = JSONObject().put("type","request").put("conversationId","c").put("requestId","req:1").put("state",state)
        store.applyEvent(event("start"));assertTrue(runCatching { store.snapshot("c") }.isFailure)
        store.applyEvent(event("end"));assertEquals(2,store.snapshot("c").messages.size)
    }
    @Test fun weakerDomCaptureCannotReplaceNetworkText() {
        seed();put("c","a","u","assistant","少了一部分","partial","dom")
        assertEquals("答复",store.snapshot("c").messages.last().text)
    }
    @Test fun activityExitUnblocksOnlyItsOwnInterruptedRequests() {
        seed("c1"); seed("c2")
        fun start(cid: String, owner: String) = store.applyEvent(JSONObject().put("type","request")
            .put("conversationId",cid).put("requestId","req:$cid").put("state","start").put("owner",owner))
        start("c1","first"); start("c2","second")
        put("c1","a","u","assistant","已收到部分","streaming")
        store.closeCapture("first")
        assertEquals("partial",store.snapshot("c1").messages.last().status)
        assertTrue(runCatching { store.snapshot("c2") }.isFailure)
    }
    @Test fun remappingTheSameProvisionalIdCannotMergeTwoServerConversations() {
        seed("local:abc");seed("other")
        fun remap(target: String) = store.applyEvent(JSONObject().put("type","remap")
            .put("from","local:abc").put("to",target))
        remap("server")
        assertTrue(runCatching { remap("other") }.isFailure)
        assertEquals(2,store.count("server")); assertEquals(2,store.count("other"))
        assertEquals("server",store.resolve("local:abc"))
    }
}
