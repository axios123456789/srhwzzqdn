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
      <div v-if="viewDictType != 3" @click="mapperConfiguration">
        <span>映射配置区</span>
      </div>
      <div
        style="border-bottom: 3px solid green; color: green"
        v-if="viewDictType == 3"
      >
        <span>映射配置区</span>
      </div>
      <div v-if="viewDictType != 4" @click="interfaceConfig">
        <span>接口配置区</span>
      </div>
      <div
        style="border-bottom: 3px solid green; color: green"
        v-if="viewDictType == 4"
      >
        <span>接口配置区</span>
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
                <el-select
                  placeholder="请选择"
                  v-model="queryDto.status"
                  style="width: 100%"
                  clearable
                >
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
        <el-button type="success" size="small" @click="addDict">
          添 加
        </el-button>
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
        <el-table-column
          label="操作"
          align="center"
          width="200"
          #default="scope"
        >
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
                <el-select
                  placeholder="请选择"
                  v-model="queryDto2.status"
                  style="width: 100%"
                  clearable
                >
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
        <el-button type="success" size="small" @click="addCode">
          添 加
        </el-button>
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
        border
      >
        <!--        default-expand-all-->

        <el-table-column
          label="操作"
          align="center"
          width="300"
          #default="scope"
        >
          <el-button type="success" size="small" @click="addCode(scope.row)">
            添加下级节点
          </el-button>
          <el-button type="primary" size="small" @click="editCode(scope.row)">
            修改
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="removeCode(scope.row.id)"
          >
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

    <!--  映射配置区展示  -->
    <div v-if="viewDictType == 3">
      <div class="search-div">
        <el-form label-width="100px" size="small">
          <el-row>
            <el-col :span="24">
              <el-form-item label="映射业务类型">
                <el-input
                  v-model="mapperConfigurationDto.type"
                  style="width: 100%"
                  clearable
                ></el-input>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="display:flex">
            <el-button
              type="primary"
              size="small"
              @click="searchMapperConfiguration"
            >
              搜索
            </el-button>
            <el-button size="small" @click="resetMapperConfigurationData">
              重置
            </el-button>
          </el-row>
        </el-form>
      </div>

      <!--添加按钮-->
      <div class="tools-div">
        <el-button type="success" size="small" @click="addMapperConfiguration">
          添 加
        </el-button>
      </div>

      <!-- 添加或修改映射配置表单对话框 -->
      <el-dialog
        v-model="dialogVisible3"
        :title="mapperConfigurationVo.id ? '添加映射配置' : '修改映射配置'"
        width="40%"
      >
        <el-form label-width="120px">
          <el-form-item label="映射业务类型">
            <el-input
              v-model="mapperConfigurationVo.type"
              placeholder=""
              clearable
            >
              <template #suffix v-if="mapperConfigurationVo.id == null">
                <el-icon @click="handleSearchClick" style="cursor: pointer;">
                  <Search />
                </el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="业务类型描述">
            <el-input
              v-model="mapperConfigurationVo.typeDescription"
              placeholder=""
            />
          </el-form-item>
          <el-form-item label="映射A类型字段1">
            <el-input
              v-model="mapperConfigurationVo.mapperFieldA1"
              placeholder=""
            />
          </el-form-item>
          <el-form-item label="映射A类型字段2">
            <el-input v-model="mapperConfigurationVo.mapperFieldA2" />
          </el-form-item>
          <el-form-item label="映射B类型字段1">
            <el-input
              v-model="mapperConfigurationVo.mapperFieldB1"
              placeholder=""
            />
          </el-form-item>
          <el-form-item label="映射B类型字段2">
            <el-input v-model="mapperConfigurationVo.mapperFieldB2" />
          </el-form-item>
          <el-form-item label="映射B类型字段3">
            <el-input v-model="mapperConfigurationVo.mapperFieldB3" />
          </el-form-item>
          <el-form-item label="映射规则描述">
            <el-input
              type="textarea"
              :rows="3"
              placeholder="请输入内容"
              v-model="mapperConfigurationVo.mapperRuleDescription"
            ></el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitMapperConfiguration">
              提交
            </el-button>
            <el-button @click="dialogVisible3 = false">取消</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <!---数据表格-->
      <el-table :data="mapperList" style="width: 100%" height="400">
        <el-table-column
          label="操作"
          align="center"
          width="200"
          #default="scope"
        >
          <el-button
            type="primary"
            size="small"
            @click="editMapperConfiguration(scope.row)"
          >
            修改
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="deleteMapperConfigurationById(scope.row.id)"
          >
            删除
          </el-button>
        </el-table-column>
        <el-table-column prop="type" label="映射业务类型" width="120" />
        <el-table-column
          prop="typeDescription"
          label="业务类型描述"
          width="180"
        />
        <el-table-column
          prop="mapperFieldA1"
          label="映射A类型字段1"
          width="150"
        />
        <el-table-column
          prop="mapperFieldA2"
          label="映射A类型字段2"
          width="150"
        />
        <el-table-column
          prop="mapperFieldB1"
          label="映射B类型字段1"
          width="150"
        />
        <el-table-column
          prop="mapperFieldB2"
          label="映射B类型字段2"
          width="150"
        />
        <el-table-column
          prop="mapperFieldB3"
          label="映射B类型字段3"
          width="150"
        />
        <el-table-column
          prop="mapperRuleDescription"
          label="映射规则描述"
          width="300"
        />
      </el-table>

      <!--分页条-->
      <el-pagination
        style="margin-top: 30px"
        v-model:current-page="mapperPageParams.page"
        v-model:page-size="mapperPageParams.limit"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="mapperFetchData"
        @current-change="mapperFetchData"
        layout="total, sizes, prev, pager, next"
        :total="mapperTotal"
      />
    </div>

    <!--  接口配置区展示  -->
    <div v-if="viewDictType == 4">
      <div class="search-div">
        <el-form label-width="100px" size="small">
          <el-row>
            <el-col :span="6">
              <el-form-item label="接口名称">
                <el-input v-model="configQueryDto.interfaceName" style="width: 100%" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="分类">
                <el-select v-model="configQueryDto.category" style="width: 100%" clearable placeholder="请选择">
                  <el-option label="股票(stock)" value="stock" />
                  <el-option label="交易(trial)" value="trial" />
                  <el-option label="基金(fund)" value="fund" />
                  <el-option label="通用(common)" value="common" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="状态">
                <el-select v-model="configQueryDto.status" style="width: 100%" clearable placeholder="请选择">
                  <el-option :key="1" label="启用" :value="1" />
                  <el-option :key="0" label="停用" :value="0" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="配置id">
                <el-input v-model="configQueryDto.id" style="width: 100%" clearable />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row style="display:flex">
            <el-button type="primary" size="small" @click="searchConfig">搜索</el-button>
            <el-button size="small" @click="resetConfigData">重置</el-button>
          </el-row>
        </el-form>
      </div>

      <div class="tools-div">
        <el-button type="success" size="small" @click="addConfig">添 加</el-button>
        <el-button type="warning" size="small" @click="handleAiFetchLatest" :loading="aiFetchLoading">
          <el-icon><MagicStick /></el-icon>
          AI自动获取最新接口
        </el-button>
      </div>

      <el-table :data="configList" style="width: 100%" height="500" border>
        <el-table-column label="操作" align="center" width="150" #default="scope">
          <el-button type="primary" size="small" @click="editConfig(scope.row)">修改</el-button>
          <el-button type="danger" size="small" @click="deleteConfig(scope.row.id)">删除</el-button>
        </el-table-column>
        <el-table-column prop="id" label="配置id" width="180" />
        <el-table-column prop="interfaceName" label="接口名称" width="150" />
        <el-table-column prop="value" label="接口URL" #default="scope" min-width="300">
          <span style="word-break: break-all; font-size: 12px">{{ scope.row.value }}</span>
        </el-table-column>
        <el-table-column prop="fallbackUrl" label="兜底URL" #default="scope" min-width="250">
          <span style="word-break: break-all; font-size: 12px; color: #999">{{ scope.row.fallbackUrl }}</span>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="status" label="状态" #default="scope" width="80">
          <span v-if="scope.row.status == 1" style="color: green">启用</span>
          <span v-else style="color: red">停用</span>
        </el-table-column>
        <el-table-column prop="description" label="描述" #default="scope" min-width="200">
          <span style="word-break: break-all; font-size: 12px">{{ scope.row.description }}</span>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
      </el-table>

      <el-pagination
        style="margin-top: 30px"
        v-model:current-page="configPageParams.page"
        v-model:page-size="configPageParams.limit"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="configFetchData"
        @current-change="configFetchData"
        layout="total, sizes, prev, pager, next"
        :total="configTotal"
      />

      <!-- 接口配置编辑弹窗 -->
      <el-dialog v-model="configDialogVisible" :title="configForm.id ? '修改接口配置' : '添加接口配置'" width="50%">
        <el-form label-width="120px">
          <el-form-item label="配置id">
            <el-input v-model="configForm.id" :disabled="!!configForm._exist" placeholder="如 stock_quote_url" />
          </el-form-item>
          <el-form-item label="接口名称">
            <el-input v-model="configForm.interfaceName" placeholder="如 实时行情接口" />
          </el-form-item>
          <el-form-item label="接口URL">
            <el-input v-model="configForm.value" type="textarea" :rows="2" placeholder="接口URL模板,含占位符如{stock_code}" />
          </el-form-item>
          <el-form-item label="兜底URL">
            <el-input v-model="configForm.fallbackUrl" type="textarea" :rows="2" placeholder="原写死URL,数据库读不到时用此兜底" />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="configForm.description" type="textarea" :rows="2" placeholder="接口描述,AI匹配用" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="configForm.category" clearable placeholder="请选择">
              <el-option label="股票(stock)" value="stock" />
              <el-option label="交易(trial)" value="trial" />
              <el-option label="基金(fund)" value="fund" />
              <el-option label="通用(common)" value="common" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="configForm.status">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitConfig">提交</el-button>
            <el-button @click="configDialogVisible = false">取消</el-button>
          </el-form-item>
        </el-form>
      </el-dialog>

      <!-- AI比对弹窗 -->
      <el-dialog v-model="aiCompareDialogVisible" title="AI获取最新接口比对" width="70%" :close-on-click-modal="false">
        <div style="margin-bottom: 10px; color: #666">
          AI已获取最新接口,请勾选需要更新的接口,点击确认后批量更新数据库
        </div>
        <el-table :data="aiCompareList" ref="aiCompareTableRef" style="width: 100%" border max-height="500" @selection-change="handleAiCompareSelectionChange">
          <el-table-column type="selection" width="55" :selectable="row => row.changed" />
          <el-table-column prop="interfaceName" label="接口名称" width="150" />
          <el-table-column prop="oldUrl" label="旧URL" #default="scope" min-width="250">
            <span style="word-break: break-all; font-size: 12px; color: #999">{{ scope.row.oldUrl }}</span>
          </el-table-column>
          <el-table-column prop="newUrl" label="新URL" #default="scope" min-width="250">
            <span style="word-break: break-all; font-size: 12px" :style="{ color: scope.row.changed ? '#e6a23c' : '#999' }">{{ scope.row.newUrl }}</span>
          </el-table-column>
          <el-table-column label="是否变化" #default="scope" width="100">
            <el-tag v-if="scope.row.changed" type="warning" size="small">已变化</el-tag>
            <el-tag v-else type="info" size="small">未变化</el-tag>
          </el-table-column>
          <el-table-column label="验证状态" #default="scope" width="200">
            <el-tag v-if="scope.row.validated === true" type="success" size="small">已验证</el-tag>
            <el-tag v-else-if="scope.row.validated === false" type="danger" size="small">未验证</el-tag>
            <el-tag v-else type="info" size="small">-</el-tag>
            <span v-if="scope.row.validateMsg" style="margin-left: 6px; font-size: 11px; color: #999">{{ scope.row.validateMsg }}</span>
          </el-table-column>
        </el-table>
        <template #footer>
          <el-button @click="aiCompareDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmAiUpdate">确认更新勾选项</el-button>
        </template>
      </el-dialog>
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
  UpdateSysCode,
  FindConfigPage,
  SaveConfig,
  UpdateConfig,
  DeleteConfigById,
  AiFetchLatestConfig,
} from '@/api/sysDict'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DeleteMapperConfigurationById,
  GetMapperConfigByConditionAndPage,
  GetMapperConfigByType,
  SaveConfiguration,
} from '@/api/mapperConfiguration'
import { MagicStick } from '@element-plus/icons-vue'
//-----------------------------------------------tab标签页切换------------------------------------
const viewDictType = ref(1)
const brainA = () => {
  viewDictType.value = 1
  fetchData()
}
const brainB = () => {
  viewDictType.value = 2
  fetchDateCode()
}
const mapperConfiguration = () => {
  viewDictType.value = 3
  mapperFetchData()
}
const interfaceConfig = () => {
  viewDictType.value = 4
  configFetchData()
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
  status: '',
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
  status: '',
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
  if (sysDict.value.type == undefined || sysDict.value.type == '') {
    ElMessage.warning('【字典类型】不能为空')
    return
  }
  if (sysDict.value.code == undefined || sysDict.value.code == '') {
    ElMessage.warning('【字典状态码】不能为空')
    return
  }
  if (sysDict.value.value == undefined || sysDict.value.value == '') {
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
const codeList = ref([]) //数据展示列表
const queryDto2 = ref({}) //条件查询参数
//获取大脑字典B区数据
const fetchDateCode = async () => {
  const { data } = await GetSysCodeLis(queryDto2.value)
  codeList.value = data
}
//查询按钮
const searchSysCode = () => {
  fetchDateCode()
}
//重置按钮
const resetCodeData = () => {
  queryDto2.value = {}
  fetchDateCode()
}
//---------------------------添加修改-----------------------------------
const sysCode = ref({}) //页面表单数据
let dialogVisible2 = ref(false) //控制模态窗口开闭
const titleName = ref()
//添加按钮点击事件
const addCode = row => {
  sysCode.value = {}
  sysCode.value.status = 1
  if (!row.id) {
    //添加
    titleName.value = '添加父级数据字典'
  } else {
    titleName.value = '添加下级数据字典'
    sysCode.value.parentId = row.id
    sysCode.value.type = row.type
  }
  dialogVisible2.value = true
}
//修改数据字典
const editCode = row => {
  sysCode.value = { ...row }
  dialogVisible2.value = true
}
//提交按钮触发
const submitCode = () => {
  if (sysCode.value.type == undefined || sysCode.value.type == '') {
    ElMessage.warning('【字典类型】不能为空')
    return
  }
  if (sysCode.value.value == undefined || sysCode.value.value == '') {
    ElMessage.warning('【字典状态码】不能为空')
    return
  }
  if (sysCode.value.text == undefined || sysCode.value.text == '') {
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
  const { code, message } = await AddSysCode(sysCode.value)
  if (code === 200) {
    dialogVisible2.value = false
    ElMessage.success(message)
    fetchDateCode()
  } else {
    ElMessage.error(message)
  }
}
//修改
const updateCodeData = async () => {
  const { code, message } = await UpdateSysCode(sysCode.value)
  if (code === 200) {
    dialogVisible2.value = false
    ElMessage.success(message)
    fetchDateCode()
  } else {
    ElMessage.error(message)
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

//----------------------------------------------------------映射配置区------------------------------------------------------
//------------------------------------列表---------------------------
const mapperList = ref([])
const mapperPageParams = ref({
  page: 1,
  limit: 10,
}) //封装分页参数
const mapperTotal = ref(0) //封装查询条数
//封装条件查询参数
const mapperConfigurationDto = ref({
  type: '',
})

//调用后端接口查询映射配置列表
const mapperFetchData = async () => {
  const { data } = await GetMapperConfigByConditionAndPage(
    mapperPageParams.value.page,
    mapperPageParams.value.limit,
    mapperConfigurationDto.value
  )
  mapperList.value = data.list
  mapperTotal.value = data.total
}

//搜索按钮点击事件
const searchMapperConfiguration = () => {
  mapperFetchData()
}
//重置按钮点击事件
const resetMapperConfigurationData = () => {
  mapperConfigurationDto.value = {}
  mapperFetchData()
}

//------------------------添加修改--------------------------
const dialogVisible3 = ref(false) //控制映射配置添加修改模态窗口
const mapperConfigurationVo = ref({}) //添加或修改表单参数

//点击添加按钮触发
const addMapperConfiguration = () => {
  mapperConfigurationVo.value = {}
  dialogVisible3.value = true
}

//点击修改按钮触发
const editMapperConfiguration = row => {
  mapperConfigurationVo.value = { ...row }
  dialogVisible3.value = true
}

//点击模态窗口中的提交按钮触发
const submitMapperConfiguration = async () => {
  //非空校验
  if (
    mapperConfigurationVo.value.type == undefined ||
    mapperConfigurationVo.value.type == ''
  ) {
    ElMessage.warning('【映射业务类型】不能为空')
    return
  }
  const { code, message } = await SaveConfiguration(mapperConfigurationVo.value)
  if (code === 200) {
    dialogVisible3.value = false
    ElMessage.success(message)
    mapperFetchData()
  } else {
    ElMessage.error(message)
  }
}

//点击添加模态窗口中的搜索图标触发
const handleSearchClick = async () => {
  const { data } = await GetMapperConfigByType(mapperConfigurationVo.value.type)
  if (data == null) {
    return
  }
  mapperConfigurationVo.value.typeDescription = data.typeDescription
  mapperConfigurationVo.value.mapperRuleDescription = data.mapperRuleDescription
}

//点击删除角色按钮后触发
const deleteMapperConfigurationById = id => {
  ElMessageBox.confirm('此操作将永久删除该记录, 是否继续?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteMapperConfigurationById(id)
    if (code === 200) {
      ElMessage.success(message)
      mapperFetchData()
    } else {
      ElMessage.error(message)
    }
  })
}

//----------------------------------------------------------接口配置区------------------------------------------------------
const configList = ref([])
const configTotal = ref(0)
const configPageParams = ref({ page: 1, limit: 10 })
const configQueryDto = ref({ interfaceName: '', category: '', status: null, id: '' })

const configFetchData = async () => {
  const { data } = await FindConfigPage(
    configPageParams.value.page,
    configPageParams.value.limit,
    configQueryDto.value
  )
  configList.value = data.list
  configTotal.value = data.total
}

const searchConfig = () => {
  configPageParams.value.page = 1
  configFetchData()
}

const resetConfigData = () => {
  configQueryDto.value = { interfaceName: '', category: '', status: null, id: '' }
  configPageParams.value.page = 1
  configFetchData()
}

const configDialogVisible = ref(false)
const configForm = ref({})

const addConfig = () => {
  configForm.value = { status: 1, _exist: false }
  configDialogVisible.value = true
}

const editConfig = row => {
  configForm.value = { ...row, _exist: true }
  configDialogVisible.value = true
}

const submitConfig = async () => {
  if (!configForm.value.id) {
    ElMessage.warning('【配置id】不能为空')
    return
  }
  if (!configForm.value.value) {
    ElMessage.warning('【接口URL】不能为空')
    return
  }
  if (configForm.value._exist) {
    const { code, message } = await UpdateConfig(configForm.value)
    if (code === 200) {
      configDialogVisible.value = false
      ElMessage.success('更新成功')
      configFetchData()
    } else {
      ElMessage.error(message || '更新失败')
    }
  } else {
    const { code, message } = await SaveConfig(configForm.value)
    if (code === 200) {
      configDialogVisible.value = false
      ElMessage.success('添加成功')
      configFetchData()
    } else {
      ElMessage.error(message || '添加失败')
    }
  }
}

const deleteConfig = id => {
  ElMessageBox.confirm('此操作将永久删除该记录, 是否继续?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteConfigById(id)
    if (code === 200) {
      ElMessage.success(message || '删除成功')
      configFetchData()
    } else {
      ElMessage.error(message || '删除失败')
    }
  })
}

// AI自动获取最新接口
const aiFetchLoading = ref(false)
const aiCompareDialogVisible = ref(false)
const aiCompareList = ref([])
const aiCompareTableRef = ref(null)
const aiCompareSelection = ref([])

const handleAiCompareSelectionChange = val => {
  aiCompareSelection.value = val
}

const handleAiFetchLatest = async () => {
  try {
    await ElMessageBox.confirm('将调用AI全量获取最新可用接口,可能需要较长时间,是否继续?', 'AI获取确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch (e) { return }

  aiFetchLoading.value = true
  const fetchMsg = ElMessage({
    message: 'AI正在获取最新接口,请耐心等待...',
    type: 'info',
    duration: 0,
    showClose: false,
  })

  try {
    const { code, data, message } = await AiFetchLatestConfig()
    fetchMsg.close()
    if (code === 200) {
      aiCompareList.value = data || []
      aiCompareDialogVisible.value = true
      const changedCount = aiCompareList.value.filter(r => r.changed).length
      ElMessage.success(`AI获取完成,共${aiCompareList.value.length}个接口,其中${changedCount}个有变化`)
    } else {
      ElMessage.error(message || 'AI获取失败')
    }
  } catch (error) {
    fetchMsg.close()
    console.error('AI获取最新接口失败:', error)
    ElMessage.error('AI获取失败,请稍后重试')
  } finally {
    aiFetchLoading.value = false
  }
}

const confirmAiUpdate = async () => {
  if (aiCompareSelection.value.length === 0) {
    ElMessage.warning('请勾选需要更新的接口')
    return
  }
  const selectedRows = aiCompareSelection.value
  const updateMsg = ElMessage({
    message: `正在更新${selectedRows.length}个接口...`,
    type: 'info',
    duration: 0,
    showClose: false,
  })
  try {
    let successCount = 0
    for (const row of selectedRows) {
      const { code } = await UpdateConfig({
        id: row.id,
        value: row.newUrl,
      })
      if (code === 200) successCount++
    }
    updateMsg.close()
    ElMessage.success(`更新完成,成功${successCount}/${selectedRows.length}个`)
    aiCompareDialogVisible.value = false
    configFetchData()
  } catch (error) {
    updateMsg.close()
    ElMessage.error('批量更新失败,请稍后重试')
  }
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
