import React from 'react';
import { Select } from 'antd';
import type { SelectProps } from 'antd';
import { TargetItem } from '../usePipelineData';

interface TargetListSelectorProps extends Omit<SelectProps, 'options'> {
  targets: TargetItem[];
  loading?: boolean;
}

const TargetListSelector: React.FC<TargetListSelectorProps> = ({
  targets,
  loading = false,
  ...restProps
}) => {
  // 将数据转换为选项格式
  const options: SelectProps['options'] = targets.map((target) => ({
    label: (
      <div className="flex items-center justify-between gap-4">
        <span className="font-medium">{target.target}</span>
        <div className="flex items-center gap-2 text-xs text-gray-500">
          <span className="bg-blue-50 text-blue-600 px-2 py-0.5 rounded">
            {target.drugCount} 药物
          </span>
        </div>
      </div>
    ),
    value: target.target,
    key: target.target
  }));

  return (
    <Select
      {...restProps}
      options={options}
      loading={loading}
      mode="multiple"
      placeholder="请选择靶点"
      allowClear
      showSearch
      filterOption={(input, option) => {
        const targetName = option?.value as string;
        return targetName.toLowerCase().includes(input.toLowerCase());
      }}
      maxTagCount="responsive"
      className="min-w-[200px]"
    />
  );
};

export default TargetListSelector;
