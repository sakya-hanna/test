export default [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/auth/ForgotPassword.vue'),
    meta: { title: '忘记密码' },
  },
  {
    path: '/sso/account',
    name: 'SSOAccount',
    component: () => import('../views/auth/SSOAccount.vue'),
    meta: { title: '统一身份认证-账号登录' },
  },
  {
    path: '/sso/sms',
    name: 'SSOSms',
    component: () => import('../views/auth/SSOSms.vue'),
    meta: { title: '统一身份认证-短信登录' },
  },
  {
    path: '/sso/qr',
    name: 'SSOQr',
    component: () => import('../views/auth/SSOQr.vue'),
    meta: { title: '统一身份认证-扫码登录' },
  },
  {
    path: '/register',
    redirect: '/company/register',
    meta: { title: '企业注册向导' },
  },
  {
    path: '/company/register-entry',
    redirect: '/company/register',
    meta: { title: '企业注册向导' },
  },
  {
    path: '/user-agreement',
    name: 'UserAgreement',
    component: () => import('../views/auth/UserAgreement.vue'),
    meta: { title: '用户服务协议' },
  },
  {
    path: '/privacy-policy',
    name: 'PrivacyPolicy',
    component: () => import('../views/auth/PrivacyPolicy.vue'),
    meta: { title: '隐私政策' },
  },
  {
    path: '/m/login',
    name: 'MobileLogin',
    component: () => import('../views/auth/MobileLogin.vue'),
    meta: { title: '移动端登录' },
  },
  {
    path: '/pad/login',
    name: 'PadLogin',
    component: () => import('../views/auth/PadLogin.vue'),
    meta: { title: '平板端登录' },
  },
]
