import axios from 'axios';
import { message } from 'antd';
import dayjs from 'dayjs';

export interface PipelineExportData {
  diseaseName: string;
  phases: string[];
  targetRows: {
    target: string;
    drugCount: number;
    avgPhaseScore: number;
    drugs: Array<{
      drugNameEn: string;
      drugNameCn?: string;
      drugId: string;
      originator: string;
      researchInstitute?: string;
      moa?: string;
      highestPhase: string;
      highestPhaseScore: number;
      highestPhaseDate?: string;
      nctId?: string;
      highestTrialId?: string;
      highestTrialPhase?: string;
    }>;
  }[];
  filterParams: {
    diseaseId: number;
    targets?: string[];
    phases?: string[];
    origins?: string[];
    sortBy?: string;
    sortOrder?: string;
  };
}

/**
 * 导出管线数据到 Excel
 * 如果后端实现了 /api/v1/progress/export 接口，则使用后端导出
 * 否则使用前端导出
 */
export const exportPipelineData = async (
  filterParams: any,
  data: PipelineExportData
): Promise<void> => {
  try {
    // 首先尝试使用后端导出
    try {
      const response = await axios.post(
        '/api/v1/progress/export',
        {
          filterParams,
          format: 'xlsx'
        },
        {
          responseType: 'blob'
        }
      );

      // 创建下载链接
      const blob = new Blob([response.data], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `靶点研发进展格局_${data.diseaseName}_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);

      message.success('导出成功');
      return;
    } catch (backendError: any) {
      // 如果后端接口不存在，则使用前端导出
      if (backendError.response?.status === 404) {
        console.info('后端导出接口未实现，使用前端导出');
      } else {
        throw backendError;
      }
    }

    // 前端导出实现
    await frontendExportPipelineData(data);
  } catch (error) {
    console.error('导出失败:', error);
    message.error('导出失败，请稍后重试');
    throw error;
  }
};

/**
 * 前端实现导出管线数据到 Excel
 * 使用纯前端方式生成 Excel 文件
 */
const frontendExportPipelineData = async (data: PipelineExportData): Promise<void> => {
  // 构建工作表数据
  const rows: any[] = [];

  // 1. 导出信息头
  rows.push(['靶点研发进展格局数据导出']);
  rows.push([`疾病名称: ${data.diseaseName}`]);
  rows.push([`导出时间: ${dayjs().format('YYYY-MM-DD HH:mm:ss')}`]);
  rows.push([`研发阶段: ${data.phases.join(', ')}`]);
  rows.push([]); // 空行

  // 2. 筛选条件
  rows.push(['筛选条件']);
  if (data.filterParams.targets?.length) {
    rows.push([`靶点: ${data.filterParams.targets.join(', ')}`]);
  }
  if (data.filterParams.phases?.length) {
    rows.push([`阶段: ${data.filterParams.phases.join(', ')}`]);
  }
  if (data.filterParams.origins?.length) {
    rows.push([`起源公司: ${data.filterParams.origins.join(', ')}`]);
  }
  rows.push([`排序: ${data.filterParams.sortBy || '默认'} ${data.filterParams.sortOrder || ''}`]);
  rows.push([]); // 空行

  // 3. 表头
  rows.push([
    '靶点名称',
    '药物数量',
    '平均阶段分值',
    '药物名称（英）',
    '药物名称（中）',
    '药物 ID',
    '研发阶段',
    '阶段分值',
    '原研机构',
    '研究机构',
    '作用机制',
    '最高试验 ID',
    '最高试验阶段',
    'NCT ID',
    '阶段日期'
  ]);

  // 4. 数据行
  data.targetRows.forEach((targetRow) => {
    targetRow.drugs.forEach((drug) => {
      rows.push([
        targetRow.target,
        targetRow.drugCount,
        targetRow.avgPhaseScore.toFixed(2),
        drug.drugNameEn,
        drug.drugNameCn || '',
        drug.drugId,
        drug.highestPhase,
        drug.highestPhaseScore.toFixed(1),
        drug.originator,
        drug.researchInstitute || '',
        drug.moa || '',
        drug.highestTrialId || '',
        drug.highestTrialPhase || '',
        drug.nctId || '',
        drug.highestPhaseDate || ''
      ]);
    });
  });

  // 5. 生成 CSV 文件（简单的纯文本导出）
  const csvContent = rows
    .map((row) =>
      row
        .map((cell) => {
          // 处理包含逗号的字段
          const cellStr = String(cell);
          if (cellStr.includes(',') || cellStr.includes('"') || cellStr.includes('\n')) {
            return `"${cellStr.replace(/"/g, '""')}"`;
          }
          return cellStr;
        })
        .join(',')
    )
    .join('\n');

  // 创建下载链接
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `靶点研发进展格局_${data.diseaseName}_${dayjs().format('YYYYMMDD_HHmmss')}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);

  message.success('导出成功（CSV 格式）');
};
