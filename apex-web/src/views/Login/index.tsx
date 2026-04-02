import { useEffect, useMemo, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { login as apiLogin, loginWithFeishuCode as apiLoginWithFeishuCode } from '@/services/auth';
import { useAuthStore } from '@/stores/authStore';
import { isFeishuClient } from '@/utils/clientEnv';

function createFeishuOauthState() {
  if (typeof window === 'undefined') {
    return `oauth_${Date.now()}`;
  }

  const cryptoApi = window.crypto;
  if (cryptoApi?.randomUUID) {
    return cryptoApi.randomUUID();
  }

  if (cryptoApi?.getRandomValues) {
    const buffer = new Uint32Array(4);
    cryptoApi.getRandomValues(buffer);
    return Array.from(buffer, value => value.toString(16).padStart(8, '0')).join('');
  }

  return `oauth_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

export default function Login() {
  const location = useLocation();
  const navigate = useNavigate();
  const { login } = useAuthStore();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const handledFeishuCodeRef = useRef<string | null>(null);
  const triggeredAutoFeishuLoginRef = useRef(false);
  const feishuAppId = import.meta.env.VITE_FEISHU_APP_ID;
  const feishuRedirectUri = import.meta.env.VITE_FEISHU_REDIRECT_URI || `${window.location.origin}/login`;
  const feishuEnabled = Boolean(feishuAppId && feishuRedirectUri);
  const searchParams = useMemo(() => new URLSearchParams(location.search), [location.search]);
  const feishuClient = useMemo(() => isFeishuClient(), []);
  const hasFeishuCode = Boolean(searchParams.get('code'));
  const showFeishuProcessing = feishuClient && feishuEnabled && (loading || !hasFeishuCode);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) { setError('请输入用户名和密码'); return; }
    setLoading(true);
    setError('');
    try {
      const result = await apiLogin({ username, password });
      if (result.code === 0 && result.data) {
        const { accessToken, user } = result.data;
        localStorage.setItem('token', accessToken);
        login(accessToken, { id: user.id, username: user.username, displayName: user.displayName });
        navigate('/target-combo');
      } else {
        setError(result.message || '登录失败');
      }
    } catch {
      setError('登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const storedState = sessionStorage.getItem('feishu_oauth_state');

    if (!code || handledFeishuCodeRef.current === code) {
      return;
    }

    if (!feishuEnabled) {
      setError('飞书登录未启用，请检查前端配置');
      return;
    }

    if (!state || !storedState || state !== storedState) {
      setError('飞书登录状态校验失败，请重新发起登录');
      return;
    }

    handledFeishuCodeRef.current = code;
    sessionStorage.removeItem('feishu_oauth_state');
    setLoading(true);
    setError('');

    apiLoginWithFeishuCode(code)
      .then(result => {
        if (result.code === 0 && result.data) {
          const { accessToken, user } = result.data;
          localStorage.setItem('token', accessToken);
          login(accessToken, { id: user.id, username: user.username, displayName: user.displayName });
          navigate('/target-combo', { replace: true });
          return;
        }
        setError(result.message || '飞书登录失败');
      })
      .catch(err => setError(err instanceof Error ? err.message : '飞书登录失败'))
      .finally(() => setLoading(false));
  }, [feishuEnabled, login, navigate, searchParams]);

  const handleFeishuLogin = () => {
    if (!feishuEnabled) {
      setError('飞书登录未配置，请检查 VITE_FEISHU_APP_ID / VITE_FEISHU_REDIRECT_URI');
      return;
    }

    const state = createFeishuOauthState();
    sessionStorage.setItem('feishu_oauth_state', state);
    const params = new URLSearchParams({
      app_id: feishuAppId,
      redirect_uri: feishuRedirectUri,
      state,
    });
    window.location.href = `https://open.feishu.cn/connect/qrconnect/page/sso?${params.toString()}`;
  };

  useEffect(() => {
    const code = searchParams.get('code');
    if (!feishuClient || !feishuEnabled || code || loading || triggeredAutoFeishuLoginRef.current) {
      return;
    }

    triggeredAutoFeishuLoginRef.current = true;
    handleFeishuLogin();
  }, [feishuClient, feishuEnabled, loading, searchParams]);

  return (
    <div className="apex-login-page">
      <div className="apex-login-card">
        <div className="apex-login-logo">
          <div className="apex-logo-mark">HB</div>
          <div>
            <div className="apex-login-title">Apex 早期靶点情报分析智能体</div>
            <div className="apex-login-subtitle">Harbour BioMed · 靶点情报分析平台</div>
            {showFeishuProcessing && (
              <div className="apex-login-subtitle">正在处理飞书登录，请稍候…</div>
            )}
          </div>
        </div>

        {showFeishuProcessing ? (
          <div className="apex-loading" style={{ padding: '32px 0 8px' }}>
            <div className="apex-spinner" />
            <span>{hasFeishuCode ? '飞书授权完成，正在登录...' : '检测到飞书环境，正在跳转授权...'}</span>
          </div>
        ) : (
          <form onSubmit={handleSubmit}>
            <div className="apex-form-item">
              <label className="apex-form-label">用户名</label>
              <input
                className="apex-input"
                type="text"
                placeholder="请输入用户名"
                value={username}
                onChange={e => setUsername(e.target.value)}
                autoComplete="username"
              />
            </div>

            <div className="apex-form-item">
              <label className="apex-form-label">密码</label>
              <input
                className="apex-input"
                type="password"
                placeholder="请输入密码"
                value={password}
                onChange={e => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </div>

            <button className="apex-login-btn" type="submit" disabled={loading}>
              {loading ? '登录中...' : '登 录'}
            </button>

            <div className="apex-login-divider">或</div>

            <button
              className="apex-login-btn apex-login-btn-secondary"
              type="button"
              disabled={loading}
              onClick={handleFeishuLogin}
            >
              {loading ? '处理中...' : '使用飞书登录'}
            </button>
          </form>
        )}

        {!showFeishuProcessing && error && <div className="apex-login-error">{error}</div>}
      </div>
    </div>
  );
}
