import request from '@/utils/request'

const base_api = '/superBrain/memoryReception/rowMemoryAccess' //原始记忆基本路径
const base_api2 = '/superBrain/memoryReception/workMemoryAccess' //工作记忆基本路径
const base_api3 = '/superBrain/memoryReception/lifeMemoryAccess' //生活记忆基本路径

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

//记忆联想
export const MemoryAssociation = (associativeMemory) => {
  return request({
    url: `${base_api}/associativeMemory`,
    method: 'post',
    data: associativeMemory
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

//根据id删除工作记忆
export const DeleteWorkMemoryById = id => {
  return request({
    url: `${base_api2}/deleteWorkMemoryById/${id}`,
    method: 'delete',
  })
}

//根据ids批量删除工作记忆
export const DeleteAllWorkMemoryByIds = ids => {
  return request({
    url: `${base_api2}/deleteAllWorkMemoryByIds`,
    method: 'post',
    data: ids,
  })
}


//------------------------------------生活记忆api--------------------------------------
//条件分页查询生活记忆列表
export const GetLifeMemoryByConditionAndPage = (current, limit, lifeMemoryDto) => {
  return request({
    url: `${base_api3}/getLifeMemoryByConditionAndPage/${current}/${limit}`,
    method: 'post',
    data: lifeMemoryDto,
  })
}

//保存生活记忆
export const SaveLifeMemory = lifeMemory => {
  return request({
    url: `${base_api3}/saveLifeMemory`,
    method: 'post',
    data: lifeMemory,
  })
}

//根据id删除生活记忆
export const DeleteLifeMemoryById = id => {
  return request({
    url: `${base_api3}/deleteLifeMemoryById/${id}`,
    method: 'delete',
  })
}

//根据ids批量删除生活记忆
export const DeleteAllLifeMemoryByIds = ids => {
  return request({
    url: `${base_api3}/deleteAllLifeMemoryByIds`,
    method: 'post',
    data: ids,
  })
}
//***********************************************************************************
