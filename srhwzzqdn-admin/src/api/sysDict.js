import request from '@/utils/request'

const base_api = '/superBrain/system/dict'

//根据type查询键值对
export const GetKeyAndValueByType = type => {
  return request({
    url: `${base_api}/getKeyAndValueByType/${type}`,
    method: 'get',
  })
}

//根据type查询对应权限的账号等级(只能是自己的下级权限)
export const GetUserLevelByPower = type => {
  return request({
    url: `${base_api}/getUserLevelByPower/${type}`,
    method: 'get',
  })
}

//条件查询数据字典列表
export const GetSysDictList = (current, limit, queryDto) => {
  return request({
    url: `${base_api}/getSysDictList/${current}/${limit}`,
    method: 'get',
    params: queryDto,
  })
}

//添加修改数据字典
export const SaveDict = sysDict => {
  return request({
    url: `${base_api}/saveDict`,
    method: 'post',
    data: sysDict,
  })
}

//删除数据字典数据
export const DeleteDictById = id => {
  return request({
    url: `${base_api}/deleteDictById/${id}`,
    method: 'delete',
  })
}

//查询中国行政区划
export const GetAdministrative = () => {
  return request({
    url: `${base_api}/getAdministrative`,
    method: 'get',
  })
}

//根据type获取t_sys_code键值对（仅只有一级数据的时候）
export const GetSysCodeByType = type => {
  return request({
    url: `${base_api}/getSysCodeByType/${type}`,
    method: 'get',
  })
}

//获取sysCode码表的数据列表
export const GetSysCodeLis = queryDto2 => {
  return request({
    url: `${base_api}/getSysCodeList`,
    method: 'get',
    params: queryDto2,
  })
}

//添加sysCode码表数据
export const AddSysCode = sysCode => {
  return request({
    url: `${base_api}/addSysCode`,
    method: 'post',
    data: sysCode,
  })
}

//修改sysCode码表数据
export const UpdateSysCode = sysCode => {
  return request({
    url: `${base_api}/updateSysCode`,
    method: 'put',
    data: sysCode,
  })
}

//根据id删除sysCode码表数据
export const DeleteSysCodeById = id => {
  return request({
    url: `${base_api}/deleteSysCodeById/${id}`,
    method: 'delete',
  })
}

//获取所有sysCode码表数据
export const GetAllSysCode = type => {
  return request({
    url: `${base_api}/getAllSysCodeByType/${type}`,
    method: 'get',
  })
}

// ============ 接口配置区（t_sys_comm_config） ============
const config_api = '/superBrain/system/sysCommConfig'

// 分页查询接口配置
export const FindConfigPage = (current, limit, queryDto) => {
  return request({
    url: `${config_api}/findPage/${current}/${limit}`,
    method: 'get',
    params: queryDto,
  })
}

// 查询全部启用的接口配置
export const FindAllEnabledConfig = () => {
  return request({
    url: `${config_api}/findAllEnabled`,
    method: 'get',
  })
}

// 根据id查询接口配置
export const FindConfigById = id => {
  return request({
    url: `${config_api}/findById/${id}`,
    method: 'get',
  })
}

// 新增接口配置
export const SaveConfig = config => {
  return request({
    url: `${config_api}/save`,
    method: 'post',
    data: config,
  })
}

// 修改接口配置
export const UpdateConfig = config => {
  return request({
    url: `${config_api}/update`,
    method: 'put',
    data: config,
  })
}

// 删除接口配置
export const DeleteConfigById = id => {
  return request({
    url: `${config_api}/deleteById/${id}`,
    method: 'delete',
  })
}

// AI自动全量获取最新可用接口
export const AiFetchLatestConfig = () => {
  return request({
    url: `${config_api}/aiFetchLatest`,
    method: 'post',
    timeout: 900000,
  })
}
