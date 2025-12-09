<template>
  <div class="rowMemoryAccessDT">
    <h1 style="margin-top: 10px; font-family: 方正姚体; color: black">&emsp;<el-icon><MagicStick /></el-icon> 原始记忆接入</h1>
    <!---搜索表单-->
    <div class="search-div">
      <el-form label-width="120px" size="small">
        <!--    第一行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="关系人名称">
              <el-input
                  v-model="rowQueryDto.contact"
                  style="width: 100%"
                  placeholder="您想要查询和谁的记忆？"
                  clearable
              ></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="关系人类型">
              <el-select
                  v-model="rowQueryDto.contactType"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in contactTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="记录时间">
              <el-date-picker
                  v-model="recordTime"
                  type="datetimerange"
                  range-separator="To"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  :editable="false"
                  clearable
              />
            </el-form-item>
          </el-col>
        </el-row>
        <!--    第二行查询条件    -->
        <el-row>
          <el-col :span="6">
            <el-form-item label="原始记忆类型">
              <el-select
                  v-model="rowQueryDto.rowMemoryType"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in rowMemoryTypeItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="原始记忆来源">
              <el-select
                  v-model="rowQueryDto.memorySource"
                  multiple
                  placeholder="请选择"
                  style="width: 100%"
                  clearable
              >
                <el-option
                    v-for="item in memorySourceItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="记忆联想状态">
              <el-select
                  v-model="rowQueryDto.memoryAssociativeStatus"
                  placeholder="请选择"
                  style="width: 100%"
                  multiple
                  clearable
              >
                <el-option
                    v-for="item in associativeStatusItem"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                ></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row style="display:flex">
          <el-button
              type="primary"
              size="small"
              @click="searchRowMemory"
              class="beautified-search-btn"
          >
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button
              size="small"
              @click="resetRowMemory"
              class="beautified-reset-btn"
          >
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-row>
      </el-form>
    </div>

    <!--总体操作按钮一览-->
    <div class="tools-div beautified-tools">
      <el-button
          type="danger"
          size="small"
          @click="deleteSelectAll"
          class="action-btn batch-delete-btn"
      >
        <el-icon><Delete /></el-icon>
        批量删除
      </el-button>
      <el-button
          type="info"
          size="small"
          @click="showExportDialog"
          class="action-btn export-btn"
      >
        <el-icon><Download /></el-icon>
        一键导出
      </el-button>
      <el-button
          type="success"
          size="small"
          @click="addRowMemory"
          class="action-btn manual-btn"
      >
        <el-icon><DocumentAdd /></el-icon>
        手动录入
      </el-button>
      <el-button
          type="warning"
          size="small"
          @click="addRowMemory2"
          class="action-btn responsive-btn"
      >
        <el-icon><DataAnalysis /></el-icon>
        响应式录入
      </el-button>
      <el-button
          type="danger"
          size="small"
          @click="addRowMemory3"
          class="action-btn ai-btn"
      >
        <el-icon><Cpu /></el-icon>
        智能录入
      </el-button>
    </div>

    <!-- 导出对话框 -->
    <el-dialog
        v-model="exportDialogVisible"
        title="导出数据"
        width="500px"
        class="export-dialog"
        :close-on-click-modal="false"
    >
      <div class="export-dialog-content">
        <el-form label-width="100px">
          <el-form-item label="导出范围">
            <el-radio-group v-model="exportScope">
              <el-radio label="current">导出当前页 ({{ rowList.length }} 条)</el-radio>
              <el-radio label="all">导出全部数据 ({{ rowTotal }} 条)</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="文件名称">
            <el-input
                v-model="exportFileName"
                placeholder="请输入导出文件名称"
                clearable
            />
          </el-form-item>

          <el-form-item label="包含列">
            <el-checkbox-group v-model="selectedColumns">
              <el-checkbox
                  v-for="column in availableColumns"
                  :key="column.key"
                  :label="column.key"
              >
                {{ column.label }}
              </el-checkbox>
            </el-checkbox-group>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="exportDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleExport" :loading="exportLoading">
            {{ exportLoading ? '导出中...' : '开始导出' }}
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!--  手动录入和修改模态窗口  -->
    <el-dialog
        v-model="dialogVisible"
        :title="rowMemory.id ? '原始记忆重塑' : '原始记忆接入'"
        width="80%"
        class="custom-dialog enhanced-dialog"
        :close-on-click-modal="false"
        :lock-scroll="false"
        align-center
        draggable
        :fullscreen="isFullscreen"
    >
      <div class="dialog-content">
        <el-form label-width="120px" class="scrollable-form">
          <el-form label-width="120px">
            <!--    第一行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆起始时间">
                  <el-date-picker
                      v-model="rowMemory.recordTime"
                      type="datetime"
                      style="width: 100%"
                      placeholder="选择日期时间"
                      :editable="false"
                      :value-format="'YYYY-MM-DD HH:mm:ss'"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记忆终止时间">
                  <el-date-picker
                      v-model="rowMemory.recordEndTime"
                      type="datetime"
                      style="width: 100%"
                      placeholder="选择日期时间"
                      :editable="false"
                      :value-format="'YYYY-MM-DD HH:mm:ss'"
                  ></el-date-picker>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第二行     -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="关系人名称">
                  <el-input
                      v-model="rowMemory.contact"
                      style="width: 100%"
                      placeholder="输入与谁发生的记忆"
                      clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="关系人类型">
                  <el-select
                      v-model="rowMemory.contactType"
                      placeholder="请选择"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in contactTypeItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第三行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆册">
                  <el-upload
                      v-model:file-list="fileList"
                      action="http://localhost:8400/superBrain/system/fileUpload"
                      list-type="picture-card"
                      multiple
                      :on-preview="handlePictureCardPreview"
                      :on-success="handleSliderSuccess"
                      :on-remove="handleRemove"
                      :headers="headers"
                  >
                    <el-icon>
                      <Plus />
                    </el-icon>
                  </el-upload>
                  <el-dialog v-model="dialogVisibleHandle">
                    <img w-full :src="dialogImageUrl" alt="">
                  </el-dialog>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第四行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆地点简称">
                  <el-select
                      v-model="rowMemory.memoryPlaceShort"
                      placeholder="请选择"
                      style="width: 100%"
                      @change="placeShortChange"
                      clearable
                  >
                    <el-option
                        v-for="item in placeShortItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记忆地点">
                  <el-cascader v-model="rowMemory.memoryPlace"
                               :options="formattedAddressOptions"
                               clearable
                               placeholder="请选择记忆地点"
                               style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第五行    -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="详细地点">
                  <el-input
                      v-model="rowMemory.memoryPlaceDetail"
                      style="width: 100%"
                      clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记忆类型">
                  <el-select
                      v-model="rowMemory.rowMemoryType"
                      placeholder="请选择"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in rowMemoryTypeItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第六行     -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆内容">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="5"
                      placeholder="请输入记忆内容（what）"
                      v-model="rowMemory.rowMemoryContent"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第七行     -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆原因">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="5"
                      placeholder="请输入产生该记忆原因（why）"
                      v-model="rowMemory.rowMemoryReason"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--    第八行    -->
            <el-row>
              <el-col :span="24">
                <el-form-item label="记忆行为">
                  <el-input
                      type="textarea"
                      style="width: 100%"
                      :rows="5"
                      placeholder="请输入针对该记忆的行为（how）"
                      v-model="rowMemory.rowMemoryAction"
                  ></el-input>
                </el-form-item>
              </el-col>
            </el-row>
            <!--   第九行     -->
            <el-row>
              <el-col :span="12">
                <el-form-item label="记忆来源">
                  <el-select
                      v-model="rowMemory.memorySource"
                      placeholder="请选择"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in memorySourceItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="记忆联想状态">
                  <el-select
                      v-model="rowMemory.memoryAssociativeStatus"
                      placeholder="请选择"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in associativeStatusItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-form>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submit">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!--  响应式录入交互模态窗口  -->
    <el-dialog
        v-model="responseDialogVisible"
        title="响应式录入原始记忆"
        :width="'60%'"
        draggable
        style="border-radius: 10px;"
    >
      <!--   记忆内容   -->
      <div style="padding: 20px;" v-if="componentControl.rowMemoryContent == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          在{{responseRowMemory.recordTime}}【上次记忆时间】到{{responseRowMemory.recordEndTime}}【当前时间】这个时间段你在做什么？
        </span>

        <el-input
            type="textarea"
            :rows="6"
            placeholder="请详细描述您在这个时间段内的活动、感受和想法..."
            v-model="responseRowMemory.rowMemoryContent"
            style="
        width: 100%;
        border-radius: 6px;
      "
        ></el-input>
      </div>

      <!--  关系人类型    -->
      <div style="padding: 20px;" v-if="componentControl.contactType == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          这条记忆是和什么人产生的？
        </span>

        <el-select
            v-model="responseRowMemory.contactType"
            placeholder="请选择和什么人产生的记忆"
            style="
              width: 100%;
              border-radius: 6px;
            "
            clearable
        >
          <el-option
              v-for="item in contactTypeItem"
              :key="item.value"
              :label="item.text"
              :value="item.value"
          ></el-option>
        </el-select>
      </div>

      <!--   关系人名称   -->
      <div style="padding: 20px;" v-if="componentControl.contact == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          这个人的名字是什么？
        </span>

        <el-input
            placeholder="请输入这段记忆关联人名称"
            v-model="responseRowMemory.contact"
            style="
              width: 100%;
              border-radius: 6px;
            "
        ></el-input>
      </div>

      <!--  记忆地点简称    -->
      <div style="padding: 20px;" v-if="componentControl.memoryPlaceShort == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          这段记忆在什么地方发生的？
        </span>

        <el-select
            v-model="responseRowMemory.memoryPlaceShort"
            placeholder="请选择地点简称"
            style="
              width: 100%;
              border-radius: 6px;
            "
            @change="placeShortChange2"
            clearable
        >
          <el-option
              v-for="item in placeShortItem"
              :key="item.value"
              :label="item.text"
              :value="item.value"
          ></el-option>
        </el-select>
      </div>

      <!--  记忆类型    -->
      <div style="padding: 20px;" v-if="componentControl.rowMemoryType == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          这是一段什么类型的记忆？
        </span>

        <el-select
            v-model="responseRowMemory.rowMemoryType"
            placeholder="请选择记忆类型"
            style="
              width: 100%;
              border-radius: 6px;
            "
            clearable
        >
          <el-option
              v-for="item in rowMemoryTypeItem"
              :key="item.value"
              :label="item.text"
              :value="item.value"
          ></el-option>
        </el-select>
      </div>

      <!--  记忆册    -->
      <div style="padding: 20px;" v-if="componentControl.memoryImages == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          请上传这段记忆痕迹的图片！
        </span>

        <el-upload
            v-model:file-list="fileList"
            action="http://localhost:8400/superBrain/system/fileUpload"
            list-type="picture-card"
            multiple
            :on-preview="handlePictureCardPreview"
            :on-success="handleSliderSuccess"
            :on-remove="handleRemove"
            :headers="headers"
        >
          <el-icon>
            <Plus />
          </el-icon>
        </el-upload>
        <el-dialog v-model="dialogVisibleHandle">
          <img w-full :src="dialogImageUrl" alt="">
        </el-dialog>
      </div>

      <!--   记忆原因   -->
      <div style="padding: 20px;" v-if="componentControl.rowMemoryReason == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          产生这段记忆的原因是什么？
        </span>

        <el-input
            type="textarea"
            :rows="6"
            placeholder="请详细描述您发生这段记忆的原因..."
            v-model="responseRowMemory.rowMemoryReason"
            style="
              width: 100%;
              border-radius: 6px;
            "
        ></el-input>
      </div>

      <!--  记忆行为    -->
      <div style="padding: 20px;" v-if="componentControl.rowMemoryAction == 'true'">
        <span style="
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #409EFF;
          text-align: center;
          margin-bottom: 20px;
          padding: 15px;
          background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
          border-radius: 8px;
          border-left: 4px solid #409EFF;
          box-shadow: 0 2px 8px rgba(64, 158, 255, 0.1);
          line-height: 1.6;
        ">
          针对这段记忆发生的事情，您是怎么做的？
        </span>

        <el-input
            type="textarea"
            :rows="6"
            placeholder="请详细描述您发生这段记忆时做了什么..."
            v-model="responseRowMemory.rowMemoryAction"
            style="
              width: 100%;
              border-radius: 6px;
            "
        ></el-input>
      </div>

      <template #footer>
        <span class="dialog-footer" style="
          display: flex;
          justify-content: center;
          align-items: center;
          padding: 16px 20px;
          border-top: 1px solid #f0f0f0;
          gap: 12px;
        ">
          <el-button v-if="componentControl.cancelControl == 'true'"
                     @click="responseDialogVisible = false"
                     style="
              border-color: #909399;
              color: #606266;
            "
          >取消</el-button>
          <el-button v-if="componentControl.upRowMemoryControl == 'true'"
              @click="upRowMemory"
              style="
              border-color: #e6a23c;
              color: #e6a23c;
            "
          >上一段</el-button>
          <el-button v-if="componentControl.nextRowMemoryControl == 'true'"
              @click="nextRowMemory"
              style="
              border-color: #67c23a;
              color: #67c23a;
            "
          >下一段</el-button>
          <el-button v-if="componentControl.achieveControl == 'true'"
              type="primary"
              @click="insertAchieve"
              style="
              background: #409EFF;
              border-color: #409EFF;
            "
          >完成</el-button>
        </span>
      </template>
    </el-dialog>


    <!--  智能录入  -->
    <el-dialog
        v-model="AIDialogVisible"
        title="智能录入原始记忆"
        :width="AiWidth"
        draggable
        style="border-radius: 12px;"
        align-center
    >
      <div style="padding: 10px 0 20px 0; text-align: center;">
        <!-- 手动配置按钮 - 优雅地放在标题右侧 -->
        <div style="position: relative; margin-bottom: 10px;" v-if="AiComponentControl.manualConfigButton == 'true'">
          <button
              style="position: absolute;
                 top: -42px;
                 right: 0;
                 padding: 8px 20px;
                 background: linear-gradient(135deg, #909399 0%, #A8A9AD 100%);
                 color: white;
                 border: none;
                 border-radius: 8px;
                 font-size: 13px;
                 font-weight: 600;
                 cursor: pointer;
                 transition: all 0.3s ease;
                 box-shadow: 0 2px 8px rgba(144, 147, 153, 0.3);
                 letter-spacing: 0.5px;"
              @mouseover="this.style.transform = 'translateY(-2px)'; this.style.boxShadow = '0 4px 12px rgba(144, 147, 153, 0.4)'; this.style.background = 'linear-gradient(135deg, #808389 0%, #98999D 100%)'"
              @mouseleave="this.style.transform = 'translateY(0)'; this.style.boxShadow = '0 2px 8px rgba(144, 147, 153, 0.3)'; this.style.background = 'linear-gradient(135deg, #909399 0%, #A8A9AD 100%)'"
              @click="manualConfig"
          >
            ⚙️ 手动配置
          </button>
        </div>

        <!-- 录入原始记忆日期选择 -->
        <div v-if="AiComponentControl.dateControl == 'true'"
             style="font-size: 16px; color: #303133; margin-bottom: 32px; line-height: 1.7; padding: 0 40px;">
          是否开始智能录入
          <span
              style="color: #409EFF; font-weight: 700; font-size: 18px; margin: 0 8px; text-shadow: 0 1px 2px rgba(64, 158, 255, 0.2);">{{ lossDateCurrent }}</span>
          这天的数据？
        </div>

        <!-- 选择录入阶段的日期 -->
        <div v-if="AiComponentControl.timePeriodControl == 'true'"
             style="font-size: 16px; color: #303133; margin-bottom: 32px; line-height: 1.7; padding: 0 40px;">
          开始智能录入
          <span
              style="color: #409EFF; font-weight: 700; font-size: 18px; margin: 0 8px; text-shadow: 0 1px 2px rgba(64, 158, 255, 0.2);">{{ curHourlyTimeSlot.begin_time }}</span>
          到
          <span
              style="color: #409EFF; font-weight: 700; font-size: 18px; margin: 0 8px; text-shadow: 0 1px 2px rgba(64, 158, 255, 0.2);">{{ curHourlyTimeSlot.end_time }}</span>
          这段时间的记忆！！！
        </div>

        <!--   手动配置提示词     -->
        <div v-if="AiComponentControl.tipManualConfig == 'true'"
             style="font-size: 16px; color: #303133; margin-bottom: 32px; line-height: 1.7; padding: 0 40px;">
          <span
              style="color: #909399; font-weight: 700; font-size: 18px; margin: 0 8px; text-shadow: 0 1px 2px rgba(144, 147, 153, 0.2);">
            {{tipManualConfig}}
          </span>
        </div>

        <!--   记忆类型提示词     -->
        <div v-if="AiComponentControl.tipMemorySelect == 'true'"
             style="font-size: 16px; color: #303133; margin-bottom: 32px; line-height: 1.7; padding: 0 40px;">
          <span
              style="color: #909399; font-weight: 700; font-size: 18px; margin: 0 8px; text-shadow: 0 1px 2px rgba(144, 147, 153, 0.2);">
            {{tipMemoryType}}
          </span>
        </div>

        <el-radio-group v-if="AiComponentControl.dateChoseResultControl == 'true'" v-model="lossDateResult"
                        style="margin-bottom: 20px;" @change="resultEvent">
          <el-radio :label="1"
                    style="margin-right: 40px; color: #67C23A; font-weight: 600; --el-radio-input-border-color-hover: #67C23A;">
            ✅ 是
          </el-radio>
          <el-radio :label="2" style="color: #909399; font-weight: 600; --el-radio-input-border-color-hover: #909399;">
            ❌ 否
          </el-radio>
        </el-radio-group>

        <button
            v-if="AiComponentControl.confirmTimeButton == 'true'"
            style="padding: 14px 48px;
               background: linear-gradient(135deg, #409EFF 0%, #66b1ff 100%);
               color: white;
               border: none;
               border-radius: 10px;
               font-size: 16px;
               font-weight: 600;
               cursor: pointer;
               transition: all 0.3s ease;
               box-shadow: 0 4px 15px rgba(64, 158, 255, 0.3);
               margin-top: 10px;
               letter-spacing: 1px;"
            @mouseover="this.style.transform = 'translateY(-2px)'; this.style.boxShadow = '0 6px 20px rgba(64, 158, 255, 0.4)'; this.style.background = 'linear-gradient(135deg, #3080E0 0%, #5599FF 100%)'"
            @mouseleave="this.style.transform = 'translateY(0)'; this.style.boxShadow = '0 4px 15px rgba(64, 158, 255, 0.3)'; this.style.background = 'linear-gradient(135deg, #409EFF 0%, #66b1ff 100%)'"
            @click="confirmTime"
        >
          🔮 确认
        </button>
      </div>
    </el-dialog>

    <!--  手动配置模态窗口  -->
    <el-dialog title="手动配置" v-model="manualConfigDialogVisible" width="500px">
      <!--   仿照手动录入打开的模态窗口，不过只有一个“记忆内容”标签和一个文本域   -->
      <el-form ref="manualConfigForm" label-width="80px">
        <el-form-item label="记忆内容">
          <el-input
            v-model="manualConfigData.fieldValue"
            type="textarea"
            :rows="4"
            placeholder="请输入内容..."
            style="width: 100%; resize: vertical;"
          ></el-input>
        </el-form-item>
      </el-form>
    </el-dialog>

    <!--  数据展示列表  -->
    <el-table :data="rowList"
              style="width: 100%" height="300"
              ref="multipleTable"
              @selection-change="handleSelectionChange">
      <el-table-column
          type="selection"
          width="40">
      </el-table-column>
      <el-table-column label="操作" align="center" width="280" #default="scope">
        <div class="action-buttons">
          <el-button
              type="primary"
              size="small"
              @click="editRowMemory(scope.row)"
              class="beautified-edit-btn"
          >
            <el-icon><Edit /></el-icon>
            记忆编辑
          </el-button>
          <el-button
              type="danger"
              size="small"
              @click="deleteRowMemoryById(scope.row)"
              class="beautified-delete-btn"
          >
            <el-icon><Delete /></el-icon>
            记忆删除
          </el-button>
          <el-button
              type="warning"
              size="small"
              @click="memoryAssociative(scope.row)"
              class="beautified-associative-btn"
          >
            <el-icon><Connection /></el-icon>
            记忆联想
          </el-button>
        </div>
      </el-table-column>
      <el-table-column prop="rowMemoryType" label="记忆类型" width="120" #default="scope">
        {{ getDisplayText(scope.row.rowMemoryType, rowMemoryTypeItem) }}
      </el-table-column>
      <el-table-column prop="recordTime" label="记录开始时间" width="180" />
      <el-table-column prop="recordEndTime" label="记录结束时间" width="180" />
      <el-table-column prop="contactType" label="关系人类型" width="120" #default="scope">
        {{ getDisplayText(scope.row.contactType, contactTypeItem) }}
      </el-table-column>
      <el-table-column prop="contact" label="关系人名称" width="120" />
      <el-table-column
          prop="memoryImages"
          label="轮播图"
          #default="scope"
          width="200"
      >
        <div style="float: left;">
          <img
              v-for="(item, index) in scope.row.memoryImages"
              :key="index"
              :src="item"
              width="50"
              height="50"
          />
        </div>
      </el-table-column>
      <el-table-column prop="memoryPlace" label="记忆地点" width="200" #default="scope">
        {{ getMemoryPlaceDisplay(scope.row) }}
      </el-table-column>
      <el-table-column prop="rowMemoryContent" label="记忆内容" width="300" />
      <el-table-column prop="rowMemoryReason" label="记忆发生原由" width="180" />
      <el-table-column prop="rowMemoryAction" label="记忆行为" width="180" />
      <el-table-column prop="memoryOwnerName" label="记忆所属人" width="120" />
      <el-table-column prop="memorySource" label="记忆来源" width="120" #default="scope">
        {{ getDisplayText(scope.row.memorySource, memorySourceItem) }}
      </el-table-column>
      <el-table-column prop="memoryAssociativeStatus" label="记忆联想状态" width="150" #default="scope">
        {{ getDisplayText(scope.row.memoryAssociativeStatus, associativeStatusItem) }}
      </el-table-column>
      <el-table-column prop="recordBy" label="记录人" width="120" />
      <el-table-column prop="updateTime" label="修改时间" width="180" />
      <el-table-column prop="updateBy" label="修改者" width="120" />
    </el-table>

    <!--分页条-->
    <el-pagination
        style="margin-top: 30px"
        v-model:current-page="rowPageParams.page"
        v-model:page-size="rowPageParams.limit"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="rowFetchData"
        @current-change="rowFetchData"
        layout="total, sizes, prev, pager, next"
        :total="rowTotal"
    />
  </div>
</template>

<script setup>
import {onMounted, ref, computed, watch, nextTick} from "vue";
import {GetAdministrative, GetKeyAndValueByType} from "@/api/sysDict";
import {
  DeleteAllRowMemoryByIds,
  DeleteRowMemoryById, GetLossRowMemoryDate, GetMemoryConfigurationByTimeType,
  GetRowMemoryByConditionAndPage,
  SaveRowMemory
} from "@/api/memoryReception";
import {useApp} from "@/pinia/modules/app";
import {ElMessage, ElMessageBox} from "element-plus";
import {GetAllMapperConfigByType} from "@/api/mapperConfiguration";
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import dayjs from 'dayjs';

//-----------------------------------------------原始记忆列表--------------------------------------------------
//列表展示数据模型
const rowList = ref([]);
//分页条数数据封装
const rowTotal = ref(0);
//分页封装
const rowPageParams = ref({page: 1, limit: 10});
//条件查询参数封装
const rowQueryDto = ref({
  contact: '',
  contactType: [],
  recordBeginTime: '',
  recordEndTime: '',
  rowMemoryType: [],
  memorySource: [],
  memoryAssociativeStatus: []
});
//记录开始结束时间参数封装
const recordTime = ref([]);
const contactTypeItem = ref([]); //封装关系人类型下拉列表参数
const rowMemoryTypeItem = ref([]); //原始记忆类型下拉列表参数封装
const memorySourceItem = ref([]); //原始记忆来源下拉列表参数封装
const associativeStatusItem = ref([]); //联想状态下拉列表参数封装
// 完整的省市区数据
const formattedAddressOptions = ref([]);
const placeShortItem = ref([]); //地点简称

// 通用方法：根据值和映射表获取中文文本
const getDisplayText = (value, mappingArray) => {
  if (!value) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}
// -----获取记忆地点显示文本-------
const getMemoryPlaceDisplay = (row) => {
  if (!row.memoryPlace && !row.memoryPlaceDetail) return '-'
  let placeText = ''
  // 处理记忆地点
  if (row.memoryPlace) {
    if (Array.isArray(row.memoryPlace)) {
      // 如果是数组，转换为中文
      placeText = row.memoryPlace.map(code =>
          getAddressTextByCode(code)
      ).filter(Boolean).join('')
    } else if (typeof row.memoryPlace === 'string' && row.memoryPlace.includes(',')) {
      // 如果是逗号分隔的字符串，拆分后转换
      placeText = row.memoryPlace.split(',').map(code =>
          getAddressTextByCode(code.trim())
      ).filter(Boolean).join('')
    } else {
      // 单个代码或直接文本
      placeText = getAddressTextByCode(row.memoryPlace) || row.memoryPlace
    }
  }
  // 拼接详细地点
  if (row.memoryPlaceDetail) {
    placeText += row.memoryPlaceDetail
  }
  return placeText || '-'
}
// 根据代码获取地址文本
const getAddressTextByCode = (code) => {
  if (!code || !formattedAddressOptions.value.length) return ''
  const findLabel = (options, targetCode) => {
    for (const option of options) {
      if (option.value == targetCode) {
        return option.label
      }
      if (option.children && option.children.length > 0) {
        const found = findLabel(option.children, targetCode)
        if (found) return found
      }
    }
    return ''
  }
  return findLabel(formattedAddressOptions.value, code)
}

//------设置默认时间为当天0点到当天23：59：59
const getTodayTimeRange = () => {
  const now = new Date();
  const beginTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);
  const endTime = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59);
  const beginTimeStr = beginTime.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
  const endTimeStr = endTime.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
  return [beginTimeStr, endTimeStr];
}

//钩子函数
onMounted(() => {
  //获取下拉列表项
  getRowMemoryTypeItem();
  getMemorySourceItem();
  getContactTypeItem();
  getAssociativeStatusItem();
  getFormattedAddressOptions();
  getPlaceShortItem();

  //设置默认时间
  const [startOfDay, endOfDay] = getTodayTimeRange();
  recordTime.value[0] = startOfDay
  recordTime.value[1] = endOfDay
  rowQueryDto.value.recordBeginTime = startOfDay;
  rowQueryDto.value.recordEndTime = endOfDay;

  rowFetchData();
});
//发送请求，获取关系人类型下拉列表
const getContactTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_ty_people_relation');
  contactTypeItem.value = data;
}
//发送请求，获取原始记忆类型下拉列表
const getRowMemoryTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_row_memory_type');
  rowMemoryTypeItem.value = data;
}
//发送请求，获取原始记忆来源下拉列表
const getMemorySourceItem = async () => {
  const { data } = await GetKeyAndValueByType('t_row_memory_source');
  memorySourceItem.value = data;
}
//获取联想状态下拉列表
const getAssociativeStatusItem = async () => {
  const { data } = await GetKeyAndValueByType('t_memory_associative_status');
  associativeStatusItem.value = data;
}
//获取中国行政区划码值对
const getFormattedAddressOptions = async () => {
  const {data} = await GetAdministrative();
  formattedAddressOptions.value = data;
}
//获取地点简称
const getPlaceShortItem = async () => {
  const { data } = await GetKeyAndValueByType('t_memory_place_short');
  placeShortItem.value = data;
}

//搜索按钮点击事件
const searchRowMemory = () => {
  rowQueryDto.value.recordBeginTime = recordTime.value[0];
  rowQueryDto.value.recordEndTime = recordTime.value[1];
  //刷新列表
  rowFetchData();
}
//重置按钮点击事件
const resetRowMemory = () => {
  rowQueryDto.value = {};
  recordTime.value = [];
  rowFetchData();
}

//发送请求，拿到原始记忆数据
const rowFetchData = async () => {
  const {data} = await GetRowMemoryByConditionAndPage(rowPageParams.value.page, rowPageParams.value.limit, rowQueryDto.value)
  //处理多张图片情况
  data.list.forEach(item => {
    if (item.memoryImages != null && item.memoryImages != ""){
      item.memoryImages = item.memoryImages.split(',')
    }else {
      item.memoryImages = []
    }
    if (item.memoryPlace != null && item.memoryPlace != ""){
      item.memoryPlace = item.memoryPlace.split(",")
    }else {
      item.memoryPlace = []
    }
  })

  rowTotal.value = data.total
  rowList.value = data.list
}

//------------------------------------------------原始记忆录入编辑-----------------------------------------------------
const rowMemory = ref({}) //原始记忆手动录入数据
const dialogVisible = ref(false) //控制手动录入和修改原始记忆模态窗口开闭
const placeShortWithPlaceMapper = ref([]) //地点简称和地点映射数据
// 全屏状态控制
const isFullscreen = ref(false);

// 切换全屏功能
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value;
};

//点击手动录入时触发
const addRowMemory = () => {
  //获取地点映射数据
  getPlaceShortWithPlaceMapper();

  rowMemory.value = {};
  rowMemory.value.memorySource = 1;
  rowMemory.value.memoryAssociativeStatus = 1;
  if (rowList.value.length > 0){
    rowMemory.value.recordTime = rowList.value[0].recordEndTime;
  }
  memoryImageList.value = [];
  fileList.value = [];
  // 重置全屏状态
  isFullscreen.value = false;
  dialogVisible.value = true;

  // 添加全屏按钮
  nextTick(() => {
    addFullscreenButton();
  });
}
//点击修改原始记忆触发
const editRowMemory = (row) => {
  //获取地点映射数据
  getPlaceShortWithPlaceMapper();

  rowMemory.value = {...row};
  fileList.value = [];
  memoryImageList.value = rowMemory.value.memoryImages;
  memoryImageList.value.forEach(url => {
    fileList.value.push({ url: url })
  })
  // 重置全屏状态
  isFullscreen.value = false;
  dialogVisible.value = true;

  // 添加全屏按钮
  nextTick(() => {
    addFullscreenButton();
  });
}

// 添加全屏按钮到标题栏
const addFullscreenButton = () => {
  const dialogHeader = document.querySelector('.enhanced-dialog .el-dialog__header');
  if (dialogHeader && !dialogHeader.querySelector('.fullscreen-btn')) {
    const fullscreenBtn = document.createElement('button');
    fullscreenBtn.className = 'el-dialog__headerbtn fullscreen-btn';
    fullscreenBtn.style.right = '50px';
    fullscreenBtn.innerHTML = `
      <i class="el-dialog__close">
        <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="16" height="16">
          <path fill="currentColor" d="m160 96.064 192 192V160h64v192H160v-64h128L160 96.064zm0 831.872 192-192v128h64V672H160v64h128L160 927.936zm704-831.872-192 192V160h-64v192h256v-64H704L864 96.064zm0 831.872-192-192v128h-64V672h256v64H704L864 927.936z"/>
        </svg>
      </i>
    `;
    fullscreenBtn.onclick = toggleFullscreen;
    dialogHeader.appendChild(fullscreenBtn);
  }
};

//获取地点映射数据
const getPlaceShortWithPlaceMapper = async () => {
  const {data} = await GetAllMapperConfigByType("shortPlaceWithPlace");
  placeShortWithPlaceMapper.value = data;
}

//记忆地点简称值改变事件
const placeShortChange = () => {
  const resultData = placeShortWithPlaceMapper.value.filter(item => item.mapperFieldA1 === rowMemory.value.memoryPlaceShort);
  rowMemory.value.memoryPlace = resultData[0].mapperFieldB1.split(",");
  rowMemory.value.memoryPlaceDetail = resultData[0].mapperFieldB3;
}

//点击手动录入和修改模态窗口中的提交按钮触发
const submit = async () => {
  //console.log("数据"+rowMemory.value.memoryImages.join(','))
  //基本验证
  if (rowMemory.value.recordTime == undefined || rowMemory.value.recordTime == ""){
    ElMessage.warning("【记忆开始时间】不能为空");
    return;
  }
  if (rowMemory.value.recordEndTime == undefined || rowMemory.value.recordEndTime == ""){
    ElMessage.warning("【记忆结束时间】不能为空");
    return;
  }
  if (rowMemory.value.rowMemoryContent == undefined || rowMemory.value.rowMemoryContent == ""){
    ElMessage.warning("【记忆内容】不能为空");
    return;
  }
  //数据处理
  if (rowMemory.value.memoryPlace != null && rowMemory.value.memoryPlace != "" && rowMemory.value.memoryPlace != undefined){
    rowMemory.value.memoryPlace = rowMemory.value.memoryPlace.join(",");
  } else {
    rowMemory.value.memoryPlace = null;
  }
  if (memoryImageList.value != null && memoryImageList.value.length > 0){
    rowMemory.value.memoryImages = memoryImageList.value.join(",");
  }else {
    rowMemory.value.memoryImages = null;
  }
  const {code, message} = await SaveRowMemory(rowMemory.value);
  if (code === 200){
    dialogVisible.value = false;
    ElMessage.success(message);
    rowFetchData();
  }else {
    ElMessage.error(message);
  }
}

//-------------------------文件上传-----------------------
const headers = {
  token: useApp().authorization.token, // 从pinia中获取token，在进行文件上传的时候将token设置到请求头中
}
// 图像上传成功以后的事件处理函数
// const handleAvatarSuccess = (response, uploadFile) => {
//   rowMemory.value.memoryImages = response.data
// }
//---上传商品轮播图图片-----
const memoryImageList = ref([])
//移除图像触发
const handleRemove = (uploadFile, uploadFiles) => {
  memoryImageList.value.splice(memoryImageList.value.indexOf(uploadFile.url), 1)
}
const fileList = ref([])
//上传成功触发
const handleSliderSuccess = (response, uploadFile) => {
  memoryImageList.value.push(response.data)
}
const dialogVisibleHandle = ref(false)
const dialogImageUrl = ref()
const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url;
  dialogVisibleHandle.value = true;
}

//-----------------------------------------响应式录入----------------------------------------------------------
const responseRowMemory = ref({}); //响应式录入参数
const responseDialogVisible = ref(false); //控制响应式录入模态窗口的开闭
const componentControl = ref({
  rowMemoryContent: "false",
  contactType: "false",
  contact: "false",
  memoryPlaceShort: "false",
  rowMemoryType: "false",
  memoryImages: "false",
  rowMemoryReason: "false",
  rowMemoryAction: "false",
  upRowMemoryControl: "false", //上一段记忆控制
  nextRowMemoryControl: "false", //下一段记忆控制
  cancelControl: "false", //取消控制
  achieveControl: "false" //完成控制
}); //组件控制，控制模态窗口的组件展示与否

// 获取当前时间并格式化为字符串
const getCurrentTimeString = () => {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
}

//记忆地点简称值改变事件
const placeShortChange2 = () => {
  const resultData = placeShortWithPlaceMapper.value.filter(item => item.mapperFieldA1 === responseRowMemory.value.memoryPlaceShort);
  responseRowMemory.value.memoryPlace = resultData[0].mapperFieldB1;//.split(",")
  responseRowMemory.value.memoryPlaceDetail = resultData[0].mapperFieldB3;
}

//点击响应式录入按钮触发事件
const addRowMemory2 = () => {
  //做清空数据处理
  responseRowMemory.value = {};
  memoryImageList.value = [];
  fileList.value = [];

  //做默认值设置处理
  responseRowMemory.value.memorySource = 2;
  responseRowMemory.value.memoryAssociativeStatus = 1;
  if (rowList.value.length > 0){
    responseRowMemory.value.recordTime = rowList.value[0].recordEndTime;
  } else {
    ElMessage.warning("需要有上一段记忆！");
    return;
  }
  responseRowMemory.value.recordEndTime = getCurrentTimeString();

  //设置起始组件记忆内容为展示，其余为隐藏
  componentControl.value.rowMemoryContent = "true";
  componentControl.value.contactType = "false";
  componentControl.value.contact = "false";
  componentControl.value.memoryPlaceShort = "false";
  componentControl.value.rowMemoryType = "false";
  componentControl.value.memoryImages = "false";
  componentControl.value.rowMemoryReason = "false";
  componentControl.value.rowMemoryAction = "false";

  //设置展示按钮
  componentControl.value.cancelControl = 'true';
  componentControl.value.nextRowMemoryControl = 'true';
  componentControl.value.upRowMemoryControl = "false";
  componentControl.value.achieveControl = "false";

  //打开模态窗口
  responseDialogVisible.value = true;
}

//上一段按钮点击事件
const upRowMemory = () => {
  if (componentControl.value.contactType == "true"){
    //设置关系人类型为隐藏
    componentControl.value.contactType = "false";
    //设置记忆内容为展示
    componentControl.value.rowMemoryContent = "true";

    //按钮设置
    componentControl.value.cancelControl = "true";
    componentControl.value.upRowMemoryControl = "false";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.contact == "true"){
    //设置关系人名称为隐藏
    componentControl.value.contact = "false";
    //设置关系人类型为展示
    componentControl.value.contactType = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.memoryPlaceShort == "true"){
    //设置记忆地点简称为隐藏
    componentControl.value.memoryPlaceShort = "false";
    //设置关系人名称为展示
    componentControl.value.contact = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.rowMemoryType == "true"){
    //设置记忆类型为隐藏
    componentControl.value.rowMemoryType = "false";
    //设置记忆地点简称为展示
    componentControl.value.memoryPlaceShort = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.memoryImages == "true"){
    //隐藏记忆册
    componentControl.value.memoryImages = "false";
    //展示记忆类型
    componentControl.value.rowMemoryType = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.rowMemoryReason == "true"){
    //隐藏记忆原因
    componentControl.value.rowMemoryReason = "false";
    //展示记忆册
    componentControl.value.memoryImages = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.rowMemoryAction == "true"){
    //隐藏记忆行为
    componentControl.value.rowMemoryAction = "false";
    //展示记忆原因
    componentControl.value.rowMemoryReason = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  }
}

//下一段按钮点击事件
const nextRowMemory = () => {
  if (componentControl.value.rowMemoryContent == "true"){
    //设置记忆内容为隐藏
    componentControl.value.rowMemoryContent = "false";
    //设置关系人类型为展示
    componentControl.value.contactType = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.contactType == "true"){
    //设置关系人类型为隐藏
    componentControl.value.contactType = "false";
    //设置关系人名称为展示
    componentControl.value.contact = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.contact == "true"){
    //获取地点映射数据
    getPlaceShortWithPlaceMapper();
    //设置关系人名称为隐藏
    componentControl.value.contact = "false";
    //设置地点简称为展示
    componentControl.value.memoryPlaceShort = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.memoryPlaceShort == "true"){
    //设置地点简称隐藏
    componentControl.value.memoryPlaceShort = "false";
    //设置记忆类型为展示
    componentControl.value.rowMemoryType = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.rowMemoryType == "true"){
    //隐藏记忆类型
    componentControl.value.rowMemoryType = "false";
    //展示记忆册
    componentControl.value.memoryImages = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.memoryImages == "true"){
    //隐藏记忆册
    componentControl.value.memoryImages = "false";
    //展示记忆原因
    componentControl.value.rowMemoryReason = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'true';
    componentControl.value.achieveControl = "false";
  } else if (componentControl.value.rowMemoryReason == "true"){
    //隐藏记忆原因
    componentControl.value.rowMemoryReason = "false";
    //展示记忆行为
    componentControl.value.rowMemoryAction = "true";

    //按钮设置
    componentControl.value.cancelControl = "false";
    componentControl.value.upRowMemoryControl = "true";
    componentControl.value.nextRowMemoryControl = 'false';
    componentControl.value.achieveControl = "true";
  }
}

//完成按钮点击事件
const insertAchieve = () => {
  //内容校验是否为空
  if (responseRowMemory.value.rowMemoryContent == undefined || responseRowMemory.value.rowMemoryContent == ""){
    ElMessage.warning("时间段内发生的事情不能为空");
    return;
  }
  //数据处理
  if (memoryImageList.value != null && memoryImageList.value.length > 0){
    responseRowMemory.value.memoryImages = memoryImageList.value.join(",");
  }else {
    responseRowMemory.value.memoryImages = null;
  }

  ElMessageBox.confirm('确定要将方才所填写记忆录入大脑吗?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const {code, message} = await SaveRowMemory(responseRowMemory.value);
    if (code === 200){
      responseDialogVisible.value = false;
      ElMessage.success(message);
      rowFetchData();
    }else {
      ElMessage.error(message);
    }
  })
}

//--------------------------------------------------------智能录入------------------------------------------------------------
const AIDialogVisible = ref(false); //控制智能录入模态窗口开闭
const AiComponentControl = ref({
  dateControl: "false",  //日期选择组件控制
  dateChoseResultControl: "false", //选择结果控制组件
  timePeriodControl: "false", //时间阶段选中提示控制组件
  confirmTimeButton: "false", //确认时间阶段按钮
  manualConfigButton: "false", //手动录入按钮
  //提示手动配置组件
  tipManualConfig: "false",
  //记忆选择提示组件
  tipMemorySelect: "false",
}); //智能录入组件展示控制
const AiRowMemory = ref({
  recordTime: "",
  recordEndTime: "",
  contactType: "",
  contact: "",
  memoryPlace: "",
  memoryPlaceDetail: "",
  memoryPlaceShort: "",
  rowMemoryType: "",
  rowMemoryContent: "",
  rowMemoryReason: "",
  rowMemoryAction: "",
  memorySource: "",
  memoryAssociativeStatus: "",
  memoryImages: ""
}); //智能录入数据接收实体类
const AiRowMemoryList = ref([]); //定义提交列表 list.value.push(AiRowMemory.value);
const rowMemoryConfigurationList = ref([]); //原始记忆配置表数据接收
const AiWidth = ref();//控制模态窗口的宽度
//点击智能录入按钮触发
const addRowMemory3 = async () => {
  //--------清空处理-----------
  //模态窗口宽度控制
  AiWidth.value = '';

  //各种数据清空
  //1.漏录日期数据
  lossRowMemoryDate.value = [];
  lossDateCurrent.value = null;
  lossDateResult.value = null;
  //2.按小时分段数据
  hourlyTimeSlots.value = [];
  curHourlyTimeSlot.value = null;
  //3.智能录入数据清空
  AiRowMemory.value = {}
  AiRowMemoryList.value = [];
  rowMemoryConfigurationList.value = [];
  //4.配置的提示词清空
  tipManualConfig.value = "";
  tipMemoryType.value = "";

  //设置展示组件
  AiComponentControl.value.dateControl = 'true'; //打开日期描述规则
  AiComponentControl.value.dateChoseResultControl = 'true'; //打开日期回答框单选框
  AiComponentControl.value.timePeriodControl = "false"; //关闭时间阶段选中提示控制组件
  AiComponentControl.value.confirmTimeButton = "false"; //关闭确认时间阶段按钮
  AiComponentControl.value.manualConfigButton = "false"; //关闭手动配置按钮组件
  AiComponentControl.value.tipManualConfig = "false"; //关闭提示手动配置组件
  AiComponentControl.value.tipMemorySelect = "false"; //关闭记忆选择提示组件

  //----------数据处理-----------
  //获取缺失录入记忆日期列表
  await getLossDate();
  //开始循环问答日期（是否开始录入***这天的记忆）
  lossDateCurrent.value = lossRowMemoryDate.value[0];
  AiWidth.value = 500;

  //console.log("loss数据"+lossRowMemoryDate.value[0])
  //打开模态窗口
  AIDialogVisible.value = true;
}

//------------1.循环漏录的日期进行问答是否要录入该日期的记忆-----------------
const lossRowMemoryDate = ref([]); //未录入日期接收列表
const lossDateCurrent = ref();//当前问答到的日期
const lossDateResult = ref();//存放日期选择结果
//获取缺失录入记忆日期列表
const getLossDate = async () => {
  const {data} = await GetLossRowMemoryDate();
  lossRowMemoryDate.value = data;
}
//点击是否单选框
const resultEvent = () => {
  if (lossDateResult.value == 1 && AiComponentControl.value.dateControl == "true"){//选择是，则开始录入该日期的记忆
    // 生成按小时分段的时间段
    hourlyTimeSlots.value = generateHourlyTimeSlots(lossDateCurrent.value);
    AiComponentControl.value.dateControl = 'false'; //关闭日期选择提示
    curHourlyTimeSlot.value = hourlyTimeSlots.value[0]; //将选中日期第一阶段赋值
    AiComponentControl.value.timePeriodControl = "true"; //打开当前日期时间提示组件
    lossDateResult.value = null; //置空选中结果
    AiComponentControl.value.dateChoseResultControl = 'false'; //关闭结果选择
    AiComponentControl.value.confirmTimeButton = "true"; //打开时间确认按钮组件
    //alert(hourlyTimeSlots.value[1].end_time)
  } else if (lossDateResult.value == 2 && AiComponentControl.value.dateControl == "true") {//选择否，继续选择下一个日期
    for (let i = 0; i < lossRowMemoryDate.value.length; i++) {
      if (lossDateCurrent.value == lossRowMemoryDate.value[i]){
        if (i < lossRowMemoryDate.value.length - 1){//非最后一个日期
          lossDateCurrent.value = lossRowMemoryDate.value[i+1];
          //await nextTick(); // 等待DOM更新
          lossDateResult.value = null
          return;
        }else {
          //关闭模态窗口
          AIDialogVisible.value = false;
          return;
        }
      }
    }
  }
}
//--------------2.循环选中日期，按小时分段记忆进行录入---------------------
// 新增：按小时分段的时间段存储
const hourlyTimeSlots = ref([]);
const curHourlyTimeSlot = ref(); //存放当前问答到的时间
// 新增：生成按小时分段的时间段函数
const generateHourlyTimeSlots = (selectedDate) => {
  const timeSlots = [];
  const date = dayjs(selectedDate);

  // 生成24个时间段，从0点到23点
  for (let i = 0; i < 24; i++) {
    const beginTime = date.hour(i).minute(0).second(0);
    const endTime = date.hour(i).minute(59).second(59);

    timeSlots.push({
      begin_time: beginTime.format('YYYY-MM-DD HH:mm:ss'),
      end_time: endTime.format('YYYY-MM-DD HH:mm:ss'),
      hour: i,
      display: `${i.toString().padStart(2, '0')}:00 - ${i.toString().padStart(2, '0')}:59`
    });
  }

  return timeSlots;
};
//--------------------3.点击确认时间段按钮进行该日期该时间段记忆录入-----------------
//定义手动配置提示词
const tipManualConfig = ref("");
//定义记忆类型提示词
const tipMemoryType = ref("");
const confirmTime = async () => {
  //console.log("数据："+hourlyTimeSlots.value[0].begin_time)
  //模态窗口宽度扩大
  AiWidth.value = 800;

  //基本记忆数据记录到AiRowMemory中
  AiRowMemory.value.recordTime = hourlyTimeSlots.value[0].begin_time;
  AiRowMemory.value.recordEndTime = hourlyTimeSlots.value[0].end_time;
  AiRowMemory.value.memorySource = 3;
  AiRowMemory.value.memoryAssociativeStatus = 1;

  //根据rowMemoryConfigurationList进行问答
  getMemoryByTimeType();
}
//根据查询到的配件记忆进行问答操作
const getMemoryByTimeType = async () => {
  //根据时间阶段类型查询记忆配置
  getMemoryConfigurationByTimeType(curHourlyTimeSlot.value.hour);
  //根据rowMemoryConfigurationList是否为空来判断使用的提示词组件
  if (rowMemoryConfigurationList.value.length > 0) {
    console.log("1")
  } else {
    //关闭时间阶段选中提示控制组件
    AiComponentControl.value.timePeriodControl = "false";
    //关闭确认按钮
    AiComponentControl.value.confirmTimeButton = "false";
    //配置手动录入提示词
    tipManualConfig.value = "当前没有配置任何记忆，请手动配置记忆！";
    //打开手动配置提示词组件
    AiComponentControl.value.tipManualConfig = "true";
    //打开手动录入按钮
    AiComponentControl.value.manualConfigButton = "true";
  }
}
//根据时间阶段类型获取配置记忆
const getMemoryConfigurationByTimeType = async (timeType) => {
  const {data} = await GetMemoryConfigurationByTimeType(timeType);
  rowMemoryConfigurationList.value = data;
}

//------------------4.点击手动配置按钮进行手动配置问答记忆------------
const manualConfigDialogVisible = ref(false); //手动配置提示窗口开闭
//手动配置数据存储
const manualConfigData = ref({});
const manualConfig = () => {
  manualConfigData.value = {};
  manualConfigDialogVisible.value = true;
}

//-----------------------------------------------------记忆删除--------------------------------------------------------------
//点击删除角色按钮后触发
const deleteRowMemoryById = row => {
  ElMessageBox.confirm('确定要从外置大脑清除这条记忆吗?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeleteRowMemoryById(row.id)
    if (code === 200) {
      ElMessage.success(message)
      rowFetchData();
    } else {
      ElMessage.error(message)
    }
  })
}

//--------------------------------------------------批量删除记忆功能-------------------------------------------------
// 选中的行数据
const selectedRows = ref([])
// 处理选中行变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 批量删除函数
const deleteSelectAll = async () => {
  if (!selectedRows.value || selectedRows.value.length === 0) {
    ElMessage.warning('请先选择要删除的记忆记录')
    return
  }

  await ElMessageBox.confirm(
      `确定要批量删除选中的 ${selectedRows.value.length} 条记忆记录吗？此操作不可恢复！`,
      '警告',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'batch-delete-confirm-btn',
        cancelButtonClass: 'batch-delete-cancel-btn'
      }
  )

  // 获取所有选中记录的ID
  const selectedIds = selectedRows.value.map(row => row.id)
  //console.log("id数据"+selectedIds)

  // 这里调用批量删除的API
  const {code, message} = await DeleteAllRowMemoryByIds(selectedIds)
  if (code === 200){
    // 刷新数据
    rowFetchData()

    // 清空选中状态
    if (multipleTable.value) {
      multipleTable.value.clearSelection()
    }
    selectedRows.value = []

    ElMessage.success(message)
  } else {
    ElMessage.error(message)
  }
}
// 获取表格引用
const multipleTable = ref(null)

//--------------------------------------------------通用Excel导出功能-------------------------------------------------
// 导出相关状态
const exportDialogVisible = ref(false)
const exportScope = ref('current') // 'current' 或 'all'
const exportFileName = ref('原始记忆数据')
const exportLoading = ref(false)
const selectedColumns = ref([])

// 可导出的列配置（通用配置，可以在其他页面复用）
const availableColumns = ref([
  { key: 'rowMemoryType', label: '记忆类型', width: 15 },
  { key: 'recordTime', label: '记录开始时间', width: 20 },
  { key: 'recordEndTime', label: '记录结束时间', width: 20 },
  { key: 'contactType', label: '关系人类型', width: 15 },
  { key: 'contact', label: '关系人名称', width: 15 },
  { key: 'memoryPlace', label: '记忆地点', width: 25 },
  { key: 'rowMemoryContent', label: '记忆内容', width: 40 },
  { key: 'rowMemoryReason', label: '记忆发生原由', width: 30 },
  { key: 'rowMemoryAction', label: '记忆行为', width: 30 },
  { key: 'memoryOwnerName', label: '记忆所属人', width: 15 },
  { key: 'memorySource', label: '记忆来源', width: 15 },
  { key: 'memoryAssociativeStatus', label: '记忆联想状态', width: 18 },
  { key: 'recordBy', label: '记录人', width: 12 },
  { key: 'updateTime', label: '修改时间', width: 20 },
  { key: 'updateBy', label: '修改者', width: 12 }
])

// 计算选中的列配置
const selectedColumnConfig = computed(() => {
  return availableColumns.value.filter(col => selectedColumns.value.includes(col.key))
})

// 初始化选中的列（默认全选）
onMounted(() => {
  selectedColumns.value = availableColumns.value.map(col => col.key)
})

// 显示导出对话框
const showExportDialog = () => {
  if (rowList.value.length === 0 && exportScope.value === 'current') {
    ElMessage.warning('当前页没有数据可导出')
    return
  }
  if (rowTotal.value === 0 && exportScope.value === 'all') {
    ElMessage.warning('没有数据可导出')
    return
  }
  exportDialogVisible.value = true
}

// 通用Excel导出函数
/**
 * 通用Excel导出函数
 * @param {Array} data - 要导出的数据
 * @param {Array} columns - 列配置
 * @param {Object} options - 选项
 * @param {Function} dataFormatter - 数据格式化函数
 */
const exportExcel = (data, columns, options = {}, dataFormatter = null) => {
  if (!data || data.length === 0) {
    throw new Error('没有数据可导出')
  }

  try {
    // 创建工作簿
    const workbook = XLSX.utils.book_new()

    // 创建工作表数据
    const worksheetData = []

    // 添加表头（第一行）
    const headerRow = columns.map(col => col.label)
    worksheetData.push(headerRow)

    // 处理数据行
    data.forEach(item => {
      const row = columns.map(col => {
        let value = item[col.key]

        // 如果有自定义格式化函数，使用它
        if (dataFormatter && typeof dataFormatter === 'function') {
          value = dataFormatter(item, col.key, value)
        }

        // 处理空值
        if (value === null || value === undefined) {
          return ''
        }

        // 处理数组
        if (Array.isArray(value)) {
          return value.join('; ')
        }

        return value
      })
      worksheetData.push(row)
    })

    // 创建工作表
    const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)

    // 设置列宽
    worksheet['!cols'] = columns.map(col => ({ wch: col.width }))

    // 设置样式
    if (worksheet['!ref']) {
      const range = XLSX.utils.decode_range(worksheet['!ref'])

      // 表头样式
      for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: 0, c: C })
        if (!worksheet[cellAddress].s) {
          worksheet[cellAddress].s = {}
        }
        worksheet[cellAddress].s = {
          font: { bold: true, color: { rgb: "FFFFFF" } },
          fill: { fgColor: { rgb: "409EFF" } },
          alignment: { horizontal: "center", vertical: "center" },
          border: {
            top: { style: "thin", color: { rgb: "000000" } },
            left: { style: "thin", color: { rgb: "000000" } },
            bottom: { style: "thin", color: { rgb: "000000" } },
            right: { style: "thin", color: { rgb: "000000" } }
          }
        }
      }

      // 数据行样式
      for (let R = 1; R <= range.e.r; ++R) {
        for (let C = range.s.c; C <= range.e.c; ++C) {
          const cellAddress = XLSX.utils.encode_cell({ r: R, c: C })
          if (!worksheet[cellAddress].s) {
            worksheet[cellAddress].s = {}
          }
          worksheet[cellAddress].s = {
            border: {
              top: { style: "thin", color: { rgb: "DDDDDD" } },
              left: { style: "thin", color: { rgb: "DDDDDD" } },
              bottom: { style: "thin", color: { rgb: "DDDDDD" } },
              right: { style: "thin", color: { rgb: "DDDDDD" } }
            },
            alignment: {
              horizontal: "left",
              vertical: "center",
              wrapText: true
            }
          }

          // 隔行变色
          if (R % 2 === 0) {
            worksheet[cellAddress].s.fill = { fgColor: { rgb: "F8F9FA" } }
          }
        }
      }
    }

    // 将工作表添加到工作簿
    const sheetName = options.sheetName || '数据'
    XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)

    return workbook
  } catch (error) {
    console.error('创建Excel失败:', error)
    throw error
  }
}

// 数据格式化函数（针对记忆数据的特殊处理）
const memoryDataFormatter = (item, key, value) => {
  switch(key) {
    case 'rowMemoryType':
      return getDisplayText(value, rowMemoryTypeItem.value)
    case 'contactType':
      return getDisplayText(value, contactTypeItem.value)
    case 'memorySource':
      return getDisplayText(value, memorySourceItem.value)
    case 'memoryAssociativeStatus':
      return getDisplayText(value, associativeStatusItem.value)
    case 'memoryPlace':
      return getMemoryPlaceDisplay(item)
    case 'memoryImages':
      return '' // 图片字段不导出
    default:
      return value
  }
}

// 获取全部数据
const fetchAllData = async () => {
  // 这里调用API获取所有数据，不分页
  const { data } = await GetRowMemoryByConditionAndPage(1, 10000, rowQueryDto.value)

  // 处理数据格式
  data.list.forEach(item => {
    if (item.memoryImages != null && item.memoryImages != ""){
      item.memoryImages = item.memoryImages.split(',')
    } else {
      item.memoryImages = []
    }
    if (item.memoryPlace != null && item.memoryPlace != ""){
      item.memoryPlace = item.memoryPlace.split(",")
    } else {
      item.memoryPlace = []
    }
  })

  return data.list
}

// 处理导出
const handleExport = async () => {
  if (selectedColumns.value.length === 0) {
    ElMessage.warning('请至少选择一列进行导出')
    return
  }

  exportLoading.value = true

  try {
    let exportData = []
    let dataCount = 0

    // 根据选择的范围获取数据
    if (exportScope.value === 'current') {
      exportData = rowList.value
      dataCount = rowList.value.length
    } else {
      // 导出全部数据
      exportData = await fetchAllData()
      dataCount = exportData.length
    }

    if (exportData.length === 0) {
      ElMessage.warning('没有数据可导出')
      return
    }

    // 生成文件名
    const fileName = exportFileName.value || '导出数据'
    const timestamp = new Date().getTime()
    const fullFileName = `${fileName}_${timestamp}.xlsx`

    // 使用通用导出函数
    const workbook = exportExcel(
        exportData,
        selectedColumnConfig.value,
        { sheetName: '原始记忆数据' },
        memoryDataFormatter
    )

    // 生成Excel文件并下载
    const excelBuffer = XLSX.write(workbook, {
      bookType: 'xlsx',
      type: 'array',
      cellStyles: true
    })

    const blob = new Blob([excelBuffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    saveAs(blob, fullFileName)

    ElMessage.success(`成功导出 ${dataCount} 条数据`)
    exportDialogVisible.value = false

  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.error('导出失败，请重试')
  } finally {
    exportLoading.value = false
  }
}

</script>

<style scoped>
.rowMemoryAccessDT {
  position: relative;
  width: 100%;
  min-height: 100vh;
  margin: 0;
  padding: 0;
  overflow: auto;
}

.rowMemoryAccessDT::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: url('src/assets/memory/green_img5.gif');
  background-size: cover;
  background-attachment: fixed;
  opacity: 0.5;
}

.rowMemoryAccessDT > * {
  position: relative;
  z-index: 1;
}

/* *****************美化搜索按钮 - 保持原有大小和位置 **************************/
.beautified-search-btn {
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: none;
  height: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  box-shadow: 0 1px 4px rgba(64, 158, 255, 0.3);
}

.beautified-search-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.4);
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
}

.beautified-search-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(64, 158, 255, 0.3);
}
/* 美化重置按钮 - 保持原有大小和位置 */
.beautified-reset-btn {
  border-radius: 4px;
  padding: 8px 15px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  border: 1px solid #dcdfe6;
  height: auto;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  background: white;
  color: #606266;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.beautified-reset-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.12);
  border-color: #409eff;
  color: #409eff;
  background: #f5f7fa;
}

.beautified-reset-btn:active {
  transform: translateY(0);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}
/****************************************************************/


/* *******************录入等按钮图标样式美化 ********************/
.beautified-search-btn .el-icon,
.beautified-reset-btn .el-icon {
  font-size: 12px;
  margin-right: 2px;
}

/* 美化工具按钮区域 */
.beautified-tools {
  margin: 20px 0;
  padding: 16px 24px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  float: right;
  clear: both;
}

.action-btn {
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  border: none;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.action-btn:active {
  transform: translateY(-1px);
}

.manual-btn {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: white;
}

.manual-btn:hover {
  background: linear-gradient(135deg, #389e0d 0%, #52c41a 100%);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.4);
}

.responsive-btn {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
  color: white;
}

.responsive-btn:hover {
  background: linear-gradient(135deg, #d46b08 0%, #fa8c16 100%);
  box-shadow: 0 6px 20px rgba(250, 140, 22, 0.4);
}

.ai-btn {
  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
  color: white;
}

.ai-btn:hover {
  background: linear-gradient(135deg, #cf1322 0%, #f5222d 100%);
  box-shadow: 0 6px 20px rgba(245, 34, 45, 0.4);
}

/* 导出按钮样式 */
.export-btn {
  background: linear-gradient(135deg, #909399 0%, #a6a9ad 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(144, 147, 153, 0.3);
}

.export-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(144, 147, 153, 0.4);
  background: linear-gradient(135deg, #7a7d81 0%, #8f9296 100%);
}

.export-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(144, 147, 153, 0.3);
}

.export-btn:disabled {
  background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
  color: #8c8c8c;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 批量删除按钮样式 */
.batch-delete-btn {
  background: linear-gradient(135deg, #f5222d 0%, #ff4d4f 100%);
  color: white;
  border: none;
  border-radius: 12px;
  padding: 12px 20px;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s ease;
  height: 40px;
  display: flex;
  align-items: center;
  gap: 8px;
  box-shadow: 0 4px 12px rgba(245, 34, 45, 0.3);
}

.batch-delete-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(245, 34, 45, 0.4);
  background: linear-gradient(135deg, #cf1322 0%, #f5222d 100%);
}

.batch-delete-btn:active {
  transform: translateY(-1px);
  box-shadow: 0 3px 10px rgba(245, 34, 45, 0.3);
}

.batch-delete-btn:disabled {
  background: linear-gradient(135deg, #d9d9d9 0%, #bfbfbf 100%);
  color: #8c8c8c;
  cursor: not-allowed;
  transform: none;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-btn .el-icon {
  font-size: 16px;
  margin-right: 4px;
}

/* 导出对话框样式 */
.export-dialog-content {
  padding: 20px 0;
}

.export-dialog-content .el-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.export-dialog-content .el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .beautified-tools {
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
    padding: 12px 16px;
  }

  .action-btn {
    width: 140px;
    justify-content: center;
  }
}
/*******************************************************************/


/* *****************原有添加修改的其他样式保持不变 ********************/
.tools-div {
  margin: 10px 0;
  padding: 10px;
  border-radius: 3px;
  background-color: transparent;
  float: right;
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
/*头像部分样式*/
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}
.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}
.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
/***************************************************************/

/* ***************************添加或修改模态窗口样式优化 - 添加全屏功能 ***********************/
:deep(.enhanced-dialog) {
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 16px 40px rgba(0, 0, 0, 0.2);
  max-height: 80vh !important;
}

:deep(.enhanced-dialog .el-dialog) {
  display: flex;
  flex-direction: column;
  max-height: 80vh !important;
  min-height: 600px;
  transition: all 0.3s ease;
}

/* 全屏状态下的样式调整 */
:deep(.enhanced-dialog.is-fullscreen) {
  max-height: 100vh !important;
  width: 100vw !important;
  margin: 0 !important;
  border-radius: 0 !important;
}

:deep(.enhanced-dialog.is-fullscreen .el-dialog) {
  width: 100% !important;
  height: 100vh !important;
  max-height: 100vh !important;
  border-radius: 0 !important;
  margin: 0 !important;
}

:deep(.enhanced-dialog.is-fullscreen .el-dialog__body) {
  max-height: calc(100vh - 120px) !important;
  flex: 1;
}

:deep(.enhanced-dialog.is-fullscreen .scrollable-form) {
  max-height: calc(100vh - 180px) !important;
}

/* 重点调整：标题栏高度优化 - 添加全屏按钮 */
:deep(.enhanced-dialog .el-dialog__header) {
  background: linear-gradient(135deg, #409eff 0%, #337ecc 100%);
  margin: 0;
  padding: 12px 60px 12px 20px; /* 右侧增加内边距为全屏按钮留空间 */
  display: flex;
  align-items: center;
  min-height: 40px; /* 显著减少头部高度 */
  position: relative;
}

:deep(.enhanced-dialog .el-dialog__title) {
  color: white;
  font-weight: 600;
  font-size: 16px; /* 适当减小标题字体 */
  line-height: 1.2;
  flex: 1;
}

/* 全屏按钮样式 */
:deep(.enhanced-dialog .el-dialog__headerbtn.fullscreen-btn) {
  position: absolute;
  top: 50% !important;
  right: 50px; /* 放在关闭按钮左侧 */
  transform: translateY(-50%);
  margin-top: 0;
  height: 24px;
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
  color: white;
}

:deep(.enhanced-dialog .el-dialog__headerbtn.fullscreen-btn:hover) {
  background: rgba(255, 255, 255, 0.1);
}

:deep(.enhanced-dialog .el-dialog__headerbtn.fullscreen-btn .el-dialog__close) {
  color: white;
  font-size: 16px;
  font-weight: normal;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.enhanced-dialog .el-dialog__headerbtn) {
  position: absolute;
  top: 50% !important;
  right: 16px;
  transform: translateY(-50%);
  margin-top: 0;
  height: 24px; /* 减小关闭按钮尺寸 */
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s ease;
}

:deep(.enhanced-dialog .el-dialog__headerbtn .el-dialog__close) {
  color: white;
  font-size: 20px; /* 减小关闭图标大小 */
  font-weight: bold;
}

:deep(.enhanced-dialog .el-dialog__body) {
  padding: 0;
  max-height: calc(80vh - 100px) !important; /* 相应调整内容区域高度 */
  overflow: hidden;
  display: flex;
  flex: 1;
}

.dialog-content {
  width: 100%;
  padding: 20px;
}

.scrollable-form {
  max-height: calc(80vh - 160px); /* 相应调整可滚动区域高度 */
  overflow-y: auto;
  padding-right: 12px;
}

/* 底部按钮区域优化 */
.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 16px 20px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

:deep(.enhanced-dialog .el-dialog__footer) {
  padding: 0;
}

/* 底部按钮样式优化 */
.dialog-footer .el-button {
  min-width: 100px;
  padding: 10px 24px;
}

/* 上传区域样式优化 */
:deep(.avatar-uploader) {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  transition: all 0.3s ease;
  height: 178px;
  width: 178px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

:deep(.avatar-uploader .avatar) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

:deep(.avatar-uploader .el-upload) {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.avatar-uploader-icon) {
  font-size: 28px;
  color: #8c939d;
}

:deep(.avatar-uploader:hover) {
  border-color: #409eff;
  box-shadow: 0 0 8px rgba(64, 158, 255, 0.3);
}

/* 表单布局优化 */
:deep(.scrollable-form .el-form-item) {
  margin-bottom: 18px;
}

:deep(.scrollable-form .el-row) {
  margin-bottom: 10px;
}

/* 单选框组样式调整 */
:deep(.scrollable-form .el-radio-group) {
  display: flex;
  gap: 15px;
  align-items: center;
  height: 32px;
}

:deep(.scrollable-form .el-radio) {
  margin-right: 0;
}

/* 文本域样式调整 */
:deep(.scrollable-form .el-textarea .el-textarea__inner) {
  min-height: 100px;
  resize: vertical;
}

/* 响应式调整 */
@media (max-height: 700px) {
  :deep(.enhanced-dialog) {
    max-height: 90vh !important;
  }

  :deep(.enhanced-dialog .el-dialog__body) {
    max-height: calc(90vh - 100px) !important;
  }

  .scrollable-form {
    max-height: calc(90vh - 160px);
  }

  :deep(.avatar-uploader) {
    height: 150px;
    width: 150px;
  }
}

/* 动画效果优化 */
:deep(.enhanced-dialog .el-dialog) {
  animation: dialog-fade-in 0.3s ease;
}

@keyframes dialog-fade-in {
  0% {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 滚动条优化 */
.scrollable-form::-webkit-scrollbar {
  width: 6px;
}

.scrollable-form::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.scrollable-form::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.scrollable-form::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
/*****************************************************************/


/* ****************** 列表操作按钮容器美化 ************************/
.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
}
/* 通用按钮样式 */
.beautified-edit-btn,
.beautified-delete-btn,
.beautified-associative-btn {
  border-radius: 8px;
  padding: 6px 12px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.3s ease;
  border: none;
  height: 28px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  min-width: 70px;
  justify-content: center;
}

.beautified-edit-btn:hover,
.beautified-delete-btn:hover,
.beautified-associative-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.beautified-edit-btn:active,
.beautified-delete-btn:active,
.beautified-associative-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 编辑按钮样式 */
.beautified-edit-btn {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  border: 1px solid #409eff;
}

.beautified-edit-btn:hover {
  background: linear-gradient(135deg, #337ecc 0%, #529ce3 100%);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  border-color: #337ecc;
}

/* 删除按钮样式 */
.beautified-delete-btn {
  background: linear-gradient(135deg, #f56c6c 0%, #f78989 100%);
  color: white;
  border: 1px solid #f56c6c;
}

.beautified-delete-btn:hover {
  background: linear-gradient(135deg, #d54c4c 0%, #e57373 100%);
  box-shadow: 0 4px 12px rgba(245, 108, 108, 0.3);
  border-color: #d54c4c;
}

/* 联想按钮样式 */
.beautified-associative-btn {
  background: linear-gradient(135deg, #e6a23c 0%, #ebb563 100%);
  color: white;
  border: 1px solid #e6a23c;
}

.beautified-associative-btn:hover {
  background: linear-gradient(135deg, #cf9233 0%, #e2a952 100%);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.3);
  border-color: #cf9233;
}

/* 按钮图标样式 */
.beautified-edit-btn .el-icon,
.beautified-delete-btn .el-icon,
.beautified-associative-btn .el-icon {
  font-size: 12px;
  margin-right: 2px;
}

/* 响应式调整 */
@media (max-width: 1366px) {
  .action-buttons {
    flex-direction: column;
    gap: 6px;
  }

  .beautified-edit-btn,
  .beautified-delete-btn,
  .beautified-associative-btn {
    min-width: 80px;
    padding: 5px 10px;
    font-size: 11px;
  }
}
/****************************************************************/
</style>