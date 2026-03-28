import request from "@/utils/request";

const base_api = '/superBrain/assetControl/assetLedger' //资产台账基本路径

//条件分页查询资产台账列表
export const GetAssetLedgerByConditionAndPage = (current, limit, assetLedgerDto) => {
  return request({
    url: `${base_api}/getAssetLedgerByConditionAndPage/${current}/${limit}`,
    method: 'post',
    data: assetLedgerDto,
  })
}

//保存资产台账
export const SaveAssetLedger = assetLedger => {
  return request({
    url: `${base_api}/saveAssetLedger`,
    method: 'post',
    data: assetLedger,
  })
}

//根据id删除资产台账
export const DeleteAssetLedgerById = id => {
  return request({
    url: `${base_api}/deleteAssetLedgerById/${id}`,
    method: 'delete',
  })
}

//根据ids批量删除资产台账
export const DeleteAllAssetLedgerByIds = ids => {
  return request({
    url: `${base_api}/deleteAllAssetLedgerByIds`,
    method: 'post',
    data: ids,
  })
}
