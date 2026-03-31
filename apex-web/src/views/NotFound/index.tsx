import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';

/**
 * 404 页面组件
 * 用于显示页面未找到的错误
 */
export default function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="flex items-center justify-center min-h-screen">
      <Result
        status="404"
        title="404"
        subTitle="抱歉，您访问的页面不存在"
        extra={
          <Button type="primary" onClick={() => navigate('/target-combo')}>
            返回首页
          </Button>
        }
      />
    </div>
  );
}
