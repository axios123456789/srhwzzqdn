import request from '@/utils/request'

const base_api = '/superBrain/system/sysPersonInfo'

//获取个人信息
export const GetPersonInfo = () => {
    return request({
        url: `${base_api}/getPersonInfo`,
        method: 'get'
    })
}

//编辑个人信息
export const EditPersonInfo = (codeBlock, personInfo) => {
    return request({
        url: `${base_api}/savePersonInfo/${codeBlock}`,
        method: 'post',
        data: personInfo
    })
}

//根据id删除校园级别块
export const DeletePersonSchoolInfoById = (id) => {
    return request({
        url: `${base_api}/deletePersonSchoolInfoById/${id}`,
        method: 'delete'
    })
}