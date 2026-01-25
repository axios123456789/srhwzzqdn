import request from '@/utils/request'

const base_api = '/superBrain/memoryReception/rowMemoryAccess' //原始记忆基本路径
const base_api2 = '/superBrain/memoryReception/workMemoryAccess' //工作记忆基本路径

//-----------------------------原始记忆api------------------------------------
//获取原始记忆列表【条件分页】
export const GetRowMemoryByConditionAndPage = (
  current,
  limit,
  rowMemoryDto
) => {
  return request({
    url: `${base_api}/getRowMemoryByConditionAndPage/${current}/${limit}`,
    method: 'post',
    data: rowMemoryDto,
  })
}

//保存原始记忆
export const SaveRowMemory = rowMemory => {
  return request({
    url: `${base_api}/saveRowMemory`,
    method: 'post',
    data: rowMemory,
  })
}

//根据id删除原始记忆
export const DeleteRowMemoryById = id => {
  return request({
    url: `${base_api}/deleteRowMemoryById/${id}`,
    method: 'delete',
  })
}

//根据ids批量删除原始记忆
export const DeleteAllRowMemoryByIds = ids => {
  return request({
    url: `${base_api}/deleteAllRowMemoryByIds`,
    method: 'post',
    data: ids,
  })
}

//获取没录原始记忆的日期
export const GetLossRowMemoryDate = () => {
  return request({
    url: `${base_api}/getLossRowMemoryDate`,
    method: 'get',
  })
}

//根据时间阶段类型获取记忆配置
export const GetMemoryConfigurationByTimeType = timePeriodType => {
  return request({
    url: `${base_api}/getMemoryConfigurationByTimeType/${timePeriodType}`,
    method: 'get',
  })
}

/*********************************************************************************/

//-----------------------------工作记忆api------------------------------------
//获取工作记忆列表【条件分页】
export const GetWorkMemoryByConditionAndPage = (
  current,
  limit,
  workMemoryDto
) => {
  return request({
    url: `${base_api2}/getWorkMemoryByConditionAndPage/${current}/${limit}`,
    method: 'post',
    data: workMemoryDto,
  })
}

//保存工作记忆
export const SaveWorkMemory = workMemory => {
  return request({
    url: `${base_api2}/saveWorkMemory`,
    method: 'post',
    data: workMemory,
  })
}
