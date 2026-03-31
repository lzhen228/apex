import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
import 'dayjs/locale/zh-cn';

dayjs.extend(relativeTime);
dayjs.locale('zh-cn');

export function formatDate(date: string | number | Date): string {
  return dayjs(date).format('YYYY-MM-DD');
}

export function formatDateTime(date: string | number | Date): string {
  return dayjs(date).format('YYYY-MM-DD HH:mm');
}

export function fromNow(date: string | number | Date): string {
  return dayjs(date).fromNow();
}
