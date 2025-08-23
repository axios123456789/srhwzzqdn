<template>
  <div class="dict">
    <!--  tab页切换处理  -->
    <div class="tab">
      <div v-if="viewDictType != 1" @click="brainA">
        <span>大脑字典A区</span>
      </div>
      <div
          style="border-bottom: 3px solid green; color: green"
          v-if="viewDictType == 1"
      >
        <span>大脑字典A区</span>
      </div>
      <div v-if="viewDictType != 2" @click="brainB">
        <span>大脑字典B区</span>
      </div>
      <div
          style="border-bottom: 3px solid green; color: green"
          v-if="viewDictType == 2"
      >
        <span>大脑字典B区</span>
      </div>
    </div>
    <!--  大脑字典A区展示  -->
    <div v-if="viewDictType == 1">
      <!---搜索表单-->
      <div class="search-div">
        <el-form label-width="70px" size="small">
          <el-row>
            <el-col :span="8">
              <el-form-item label="字典值">
                <el-input
                    v-model="queryDto.value"
                    style="width: 100%"
                    clearable
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="字典类型">
                <el-input
                    v-model="queryDto.type"
                    style="width: 100%"
                    clearable
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态">
                <el-select placeholder="请选择" v-model="queryDto.status" style="width: 100%" clearable>
                  <el-option :key="1" label="正常" :value="1">正常</el-option>
                  <el-option :key="0" label="停用" :value="0">停用</el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="display:flex">
            <el-button type="primary" size="small" @click="searchSysDict">
              搜索
            </el-button>
            <el-button size="small" @click="resetData">重置</el-button>
          </el-row>
        </el-form>
      </div>

      <!--添加按钮-->
      <div class="tools-div">
        <el-button type="success" size="small" @click="addDict">添 加</el-button>
      </div>

      <!-- 添加或修改数据字典表单对话框 -->
      <el-dialog v-model="dialogVisible" title="添加或修改数据字典" width="30%">
        <el-form label-width="80px">
          <el-form-item label="字典类型">
            <el-input v-model="sysDict.type" placeholder="" />
          </el-form-item>
          <el-form-item label="字典码值">
            <el-input v-model="sysDict.code" placeholder="" />
          </el-form-item>
          <el-form-item label="字典值">
            <el-input v-model="sysDict.value" placeholder="" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="sysDict.status">
              <el-radio :label="1">正常</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="描述">
            <el-input
                type="textarea"
                :rows="3"
                placeholder="请输入内容"
                v-model="sysDict.description"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submit">提交</el-button>
            <el-button @click="dialogVisible = false">取消</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <!---数据表格-->
      <el-table :data="list" style="width: 100%">
        <el-table-column label="操作" align="center" width="200" #default="scope">
          <el-button type="primary" size="small" @click="editDict(scope.row)">
            修改
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="deleteDictById(scope.row)"
          >
            删除
          </el-button>
        </el-table-column>
        <el-table-column prop="type" label="字典类型" width="100" />
        <el-table-column prop="code" label="字典码值" width="120" />
        <el-table-column prop="value" label="字典值" width="120" />
        <el-table-column prop="sex" label="状态" #default="scope" width="100">
          <span v-if="scope.row.status == 1" style="color: green">正常</span>
          <span v-else style="color: red">停用</span>
        </el-table-column>
        <el-table-column prop="description" label="描述" width="200" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="createBy" label="创建者" width="120" />
        <el-table-column prop="updateTime" label="修改时间" width="180" />
        <el-table-column prop="updateBy" label="修改者" width="120" />
      </el-table>

      <!--分页条-->
      <el-pagination
          style="margin-top: 30px"
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.limit"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="fetchData"
          @current-change="fetchData"
          layout="total, sizes, prev, pager, next"
          :total="total"
      />
    </div>

    <!--  大脑字典B区展示  -->
    <div v-if="viewDictType == 2">
      <div class="search-div">
        <el-form label-width="70px" size="small">
          <el-row>
            <el-col :span="8">
              <el-form-item label="字典值">
                <el-input
                    v-model="queryDto2.value"
                    style="width: 100%"
                    clearable
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="字典类型">
                <el-input
                    v-model="queryDto2.type"
                    style="width: 100%"
                    clearable
                ></el-input>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="状态">
                <el-select placeholder="请选择" v-model="queryDto2.status" style="width: 100%" clearable>
                  <el-option :key="1" label="正常" :value="1">正常</el-option>
                  <el-option :key="0" label="停用" :value="0">停用</el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="display:flex">
            <el-button type="primary" size="small" @click="searchSysCode">
              搜索
            </el-button>
            <el-button size="small" @click="resetCodeData">重置</el-button>
          </el-row>
        </el-form>
      </div>

      <!--添加按钮-->
      <div class="tools-div">
        <el-button type="success" size="small" @click="addCode">添 加</el-button>
      </div>

      <!-- 添加或修改数据字典表单对话框 -->
      <el-dialog v-model="dialogVisible2" :title="titleName" width="30%">
        <el-form label-width="80px">
          <el-form-item label="字典类型">
            <el-input v-model="sysCode.type" placeholder="" />
          </el-form-item>
          <el-form-item label="字典码值">
            <el-input v-model="sysCode.value" placeholder="" />
          </el-form-item>
          <el-form-item label="字典值">
            <el-input v-model="sysCode.text" placeholder="" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input v-model="sysCode.sortValue" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="sysCode.status">
              <el-radio :label="1">正常</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitCode">提交</el-button>
            <el-button @click="dialogVisible2 = false">取消</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <el-table
          :data="codeList"
          style="width: 100%; margin-bottom: 20px"
          row-key="id"
          border >
        <!--        default-expand-all-->

        <el-table-column label="操作" align="center" width="300" #default="scope">
          <el-button type="success" size="small" @click="addCode(scope.row)">
            添加下级节点
          </el-button>
          <el-button type="primary" size="small" @click="editCode(scope.row)">
            修改
          </el-button>
          <el-button type="danger" size="small" @click="removeCode(scope.row.id)">
            删除
          </el-button>
        </el-table-column>
        <el-table-column prop="value" label="字典码值" />
        <el-table-column prop="text" label="字典值" />
        <el-table-column prop="sortValue" label="排序" />
        <el-table-column prop="status" label="状态" #default="scope">
          <span v-if="scope.row.status == 1" style="color: green">正常</span>
          <span v-else style="color: red">停用</span>
        </el-table-column>
        <el-table-column prop="type" label="类型" />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import {
  AddSysCode,
  DeleteDictById,
  DeleteSysCodeById,
  GetSysCodeLis,
  GetSysDictList,
  SaveDict,
  UpdateSysCode
} from '@/api/sysDict'
import { ElMessage, ElMessageBox } from 'element-plus'
//-----------------------------------------------tab标签页切换------------------------------------
const viewDictType = ref(1);
const brainA = () => {
  viewDictType.value = 1;
  fetchData();
}
const brainB = () => {
  viewDictType.value = 2;
  fetchDateCode();
}

//-----------------------------------------------查询数据字典列表----------------------------------
//表格数据模型
const list = ref([])

//分页条数据模型
const total = ref(0)

//分页
const pageParamsForm = {
  page: 1,
  limit: 10,
}
const pageParams = ref(pageParamsForm)

//封装条件数据模型
const queryDto = ref({
  value: '',
  type: '',
  status: ''
})

//钩子函数
onMounted(() => {
  //展示列表数据
  fetchData()
})

//搜索方法
const searchSysDict = () => {
  fetchData()
}

//重置方法
const resetData = () => {
  queryDto.value = {}
  fetchData()
}

//列表方法：axios请求调用接口得到数据
const fetchData = async () => {
  const { data, code, message } = await GetSysDictList(
      pageParams.value.page,
      pageParams.value.limit,
      queryDto.value
  )
  list.value = data.list
  total.value = data.total
}

//----------------------------------------------------添加修改-------------------------------------------
const sysDict = ref({
  type: '',
  code: '',
  value: '',
  description: '',
  status: ''
})

const dialogVisible = ref(false)

//点击添加按钮后触发
const addDict = () => {
  sysDict.value = {}
  sysDict.value.status = 1
  dialogVisible.value = true
}

//点击修改后触发
const editDict = row => {
  sysDict.value = { ...row }
  dialogVisible.value = true
}

//点击添加或修改模态窗口中的提交按钮后触发
const submit = async () => {
  if (sysDict.value.type == undefined || sysDict.value.type == "") {
    ElMessage.warning('【字典类型】不能为空')
    return
  }
  if (sysDict.value.code == undefined || sysDict.value.code == "") {
    ElMessage.warning('【字典状态码】不能为空')
    return
  }
  if (sysDict.value.value == undefined || sysDict.value.value == "") {
    ElMessage.warning('【字典值】不能为空')
    return
  }
  const { code, message } = await SaveDict(sysDict.value)
  if (code === 200) {
    //关闭弹窗
    dialogVisible.value = false

    //提升消息
    ElMessage.success(message)

    //刷新页面
    fetchData()
  } else {
    ElMessage.error(message)
  }
}

//------------------------------------------------删除--------------------------------------------
//点击删除角色按钮后触发
const deleteDictById = row => {
  ElMessageBox.confirm('此操作将永久删除该记录, 是否继续?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteDictById(row.id)
    if (code === 200) {
      ElMessage.success(message)
      fetchData()
    } else {
      ElMessage.error(message)
    }
  })
}

//-------------------------------------------------------大脑字典B区----------------------------------------------------
//----------------------------查询列表----------------------------------
const codeList = ref([]); //数据展示列表
const queryDto2 = ref({}); //条件查询参数
//获取大脑字典B区数据
const fetchDateCode = async () => {
  const {data} = await GetSysCodeLis(queryDto2.value);
  codeList.value = data;
}
//查询按钮
const searchSysCode = () => {
  fetchDateCode();
}
//重置按钮
const resetCodeData = () => {
  queryDto2.value = {};
  fetchDateCode();
}
//---------------------------添加修改-----------------------------------
const sysCode = ref({}); //页面表单数据
const dialogVisible2 = ref(false); //控制模态窗口开闭
const titleName = ref();
//添加按钮点击事件
const addCode = row => {
  sysCode.value = {};
  sysCode.value.status = 1;
  if (!row.id){//添加
    titleName.value = '添加父级数据字典';
  }else {
    titleName.value = '添加下级数据字典';
    sysCode.value.parentId = row.id;
    sysCode.value.type = row.type;
  }
  dialogVisible2.value = true;
}
//修改数据字典
const editCode = (row) => {
  sysCode.value = {...row};
  dialogVisible2.value = true;
}
//提交按钮触发
const submitCode = () => {
  if (sysCode.value.type == undefined || sysCode.value.type == "") {
    ElMessage.warning('【字典类型】不能为空')
    return
  }
  if (sysCode.value.value == undefined || sysCode.value.value == "") {
    ElMessage.warning('【字典状态码】不能为空')
    return
  }
  if (sysCode.value.text == undefined || sysCode.value.text == "") {
    ElMessage.warning('【字典值】不能为空')
    return
  }

  if (!sysCode.value.id) {
    if (!sysCode.value.parentId) {
      sysCode.value.parentId = '0'
    }
    saveCodeData()
  } else {
    updateCodeData()
  }
}
//新增
const saveCodeData = async () => {
  const {code, message} = await AddSysCode(sysCode.value);
  if (code === 200){
    dialogVisible2.value = false;
    ElMessage.success(message);
    fetchDateCode();
  }else {
    ElMessage.error(message);
  }
}
//修改
const updateCodeData = async () => {
  const {code, message} = await UpdateSysCode(sysCode.value);
  if (code === 200){
    dialogVisible2.value = false;
    ElMessage.success(message);
    fetchDateCode();
  }else {
    ElMessage.error(message);
  }
}
//-------------------------------------B区大脑字典删除---------------------------------------
//点击删除角色按钮后触发
const removeCode = id => {
  ElMessageBox.confirm('此操作将永久删除该记录, 是否继续?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteSysCodeById(id)
    if (code === 200) {
      ElMessage.success(message)
      fetchDateCode()
    } else {
      ElMessage.error(message)
    }
  })
}

</script>

<style scoped>
.dict {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

.dict::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('src/assets/system/brainJt.png');
  background-size: cover;
  background-attachment: fixed;
  opacity: 0.5; /* 设置背景图片的透明度为50% */
  /*z-index: -1; !* 确保伪元素在内容下方 *!*/
}
.dict > * {
  position: relative;
  z-index: 1; /* 确保内容在伪元素上方 */
}

.tools-div {
  margin: 10px 0;
  padding: 10px;
  /*border: 1px solid #ebeef5;*/
  border-radius: 3px;
  background-color: transparent;
}

/deep/ .el-table,
/deep/ .el-table__expanded-cell {
  background-color: transparent;
  color: #001528;
  border: 1px solid;
}
/deep/ .el-table th,
/deep/ .el-table tr,
/deep/ .el-table td {
  background-color: transparent;
  color: #001528;
  border: 1px solid;
}

.search-div {
  margin-bottom: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 3px;
  background-color: transparent;
}

.tab {
  width: 100%;
  height: 40px;
  border-bottom: black;
}
.tab div {
  width: 16%;
  height: 30px;
  margin-top: 10px;
  float: left;
  text-align: center;
  color: black;
  font-size: 15px;
  font-family: 微软雅黑;
  font-weight: bold;
}
.tab div:hover {
  color: green;
}
</style>
