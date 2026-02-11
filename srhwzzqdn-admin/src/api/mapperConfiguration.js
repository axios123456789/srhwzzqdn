import request from '@/utils/request'

const base_api = '/superBrain/system/mapperConfig'

//条件分页获取映射列表
export const GetMapperConfigByConditionAndPage = (current, limit, type) => {
  return request({
    url: `${base_api}/getMapperConfigByConditionAndPage/${current}/${limit}`,
    method: 'get',
    params: type,
  })
}

//保存映射配置
export const SaveConfiguration = mapperConfiguration => {
  return request({
    url: `${base_api}/saveMapperConfig`,
    method: 'post',
    data: mapperConfiguration,
  })
}

//根据id删除映射配置
export const DeleteMapperConfigurationById = id => {
  return request({
    url: `${base_api}/deleteMapperConfigById/${id}`,
    method: 'delete',
  })
}

//根据类型查询1条记录
export const GetMapperConfigByType = type => {
  return request({
    url: `${base_api}/getMapperConfigByType/${type}`,
    method: 'get',
  })
}

//根据类型查询所有记录
export const GetAllMapperConfigByType = type => {
  return request({
    url: `${base_api}/getAllMapperConfigByType/${type}`,
    method: 'get',
  })
}
