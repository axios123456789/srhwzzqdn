import request from "@/utils/request";

const base_api = '/superBrain/assetControl/assetAccounting' //资产记账基本路径

//条件分页查询资产记账列表
export const GetAssetAccountingByConditionAndPage = (current, limit, assetAccountingDto) => {
  return request({
    url: `${base_api}/getAssetTransactionListByConditionAndPage/${current}/${limit}`,
    method: 'post',
    data: assetAccountingDto,
  })
}

//保存资产记账
export const SaveAssetAccounting = assetAccounting => {
  return request({
    url: `${base_api}/saveAssetAccounting`,
    method: 'post',
    data: assetAccounting,
  })
}

//根据id删除资产记账
export const DeleteAssetAccountingById = id => {
  return request({
    url: `${base_api}/deleteAssetAccountingById/${id}`,
    method: 'delete',
  })
}

//根据ids批量删除资产记账
export const DeleteAllAssetAccountingByIds = ids => {
  return request({
    url: `${base_api}/deleteAllAssetAccountingByIds`,
    method: 'post',
    data: ids,
  })
}
