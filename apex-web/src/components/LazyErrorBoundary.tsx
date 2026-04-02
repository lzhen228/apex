import { Component, type ReactNode } from 'react';
import { Button, Result } from 'antd';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  isChunkError: boolean;
}

/**
 * 捕获 React.lazy 动态 import 失败（ChunkLoadError）。
 * 页面 chunk 找不到时，显示刷新按钮让用户重新加载，
 * 而不是白屏崩溃。
 */
export default class LazyErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, isChunkError: false };
  }

  static getDerivedStateFromError(error: Error): State {
    const isChunkError =
      error.message.includes('Failed to fetch dynamically imported module') ||
      error.message.includes('Importing a module script failed') ||
      error.name === 'ChunkLoadError';
    return { hasError: true, isChunkError };
  }

  handleReload = () => {
    // hard reload 确保拉取最新 chunk
    window.location.reload();
  };

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
          <Result
            status="warning"
            title={this.state.isChunkError ? '页面资源加载失败' : '页面出现了错误'}
            subTitle={
              this.state.isChunkError
                ? '可能是部署了新版本，请刷新页面后重试'
                : '请刷新页面或联系管理员'
            }
            extra={
              <Button type="primary" onClick={this.handleReload}>
                刷新页面
              </Button>
            }
          />
        </div>
      );
    }
    return this.props.children;
  }
}
