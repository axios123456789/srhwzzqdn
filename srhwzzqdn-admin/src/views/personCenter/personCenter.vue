<template>
  <div class="profile-container">
    <!-- 动态背景图容器 -->
    <div class="bg-container">
      <img class="bg-gif" src="@/assets/system/wzzqdn.png" alt="background">
    </div>

    <div class="profile-content">
      <!-- 顶部标题和返回按钮 -->
      <div class="profile-header">
        <div class="header-left">
          <el-button type="primary" icon="ArrowLeft" @click="goBack">返回</el-button>
        </div>
        <h1 class="title">个人画像</h1>
        <div class="header-right">
          <el-button type="success" icon="Download" @click="exportToWord">一键导出</el-button>
        </div>
      </div>

      <!-- 资料卡片容器 -->
      <div class="profile-card">
        <!-- 基础信息块 -->
        <el-card class="info-section">
          <template #header>
            <div class="section-header">
              <el-icon>
                <User/>
              </el-icon>
              <span>基础信息</span>
              <el-button type="primary" text @click="editSection('basic', '基础')">编辑</el-button>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <div class="avatar-container">
                <el-avatar :size="120" :src="profile.personPicture || ''">
                  <span v-if="!profile.personPicture" class="empty-avatar">未上传</span>
                </el-avatar>
              </div>
            </div>
            <div class="info-item">
              <label>主人姓名</label>
              <div class="info-value" :class="{'empty-data': !profile.name}">
                <template v-if="profile.name">
                  {{ profile.name }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>性别</label>
              <div class="info-value" :class="{'empty-data': !profile.gender}">
                <template v-if="displayGender">
                  {{ displayGender }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>出生日期</label>
              <div class="info-value" :class="{'empty-data': !profile.birthDate}">
                <template v-if="profile.birthDate">
                  {{ profile.birthDate }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>年龄</label>
              <div class="info-value" :class="{'empty-data': !profile.birthDate}">
                <template v-if="age">
                  {{ age }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>身高（cm）</label>
              <div class="info-value" :class="{'empty-data': !profile.height}">
                <template v-if="profile.height">
                  {{ profile.height }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>体重（kg）</label>
              <div class="info-value" :class="{'empty-data': !profile.weight}">
                <template v-if="profile.weight">
                  {{ profile.weight }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>身份证号</label>
              <div class="info-value" :class="{'empty-data': !profile.idNumber}">
                <template v-if="profile.idNumber">
                  **************{{ profile.idNumber.slice(-4) }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>民族</label>
              <div class="info-value" :class="{'empty-data': !profile.ethnicity}">
                <template v-if="displayEthnicity">
                  {{ displayEthnicity }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>婚姻状况</label>
              <div class="info-value" :class="{'empty-data': !profile.maritalStatus}">
                <template v-if="displayMaritalStatus">
                  {{ displayMaritalStatus }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>政治面貌</label>
              <div class="info-value" :class="{'empty-data': !profile.politicalStatus}">
                <template v-if="displayPoliticalStatus">
                  {{ displayPoliticalStatus }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 联系方式块 -->
        <el-card class="info-section">
          <template #header>
            <div class="section-header">
              <el-icon>
                <Iphone/>
              </el-icon>
              <span>联系方式</span>
              <el-button type="primary" text @click="editSection('contact', '联系方式')">编辑</el-button>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <label>手机号码</label>
              <div class="info-value" :class="{'empty-data': !profile.phone}">
                <template v-if="profile.phone">
                  {{ profile.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>电子邮箱</label>
              <div class="info-value" :class="{'empty-data': !profile.email}">
                <template v-if="profile.email">
                  {{ profile.email }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>微信号</label>
              <div class="info-value" :class="{'empty-data': !profile.wechat}">
                <template v-if="profile.wechat">
                  {{ profile.wechat }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>抖音号</label>
              <div class="info-value" :class="{'empty-data': !profile.douyin}">
                <template v-if="profile.douyin">
                  {{ profile.douyin }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>籍贯地址</label>
              <div class="info-value" :class="{'empty-data': !profile.hometownAddressText && !profile.hometownAddressList}">
                <template v-if="profile.hometownAddressText || profile.hometownAddressList">
                  {{ displayHometownAddress }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>现居住地址</label>
              <div class="info-value" :class="{'empty-data': !profile.currentAddressText && !profile.currentAddressList}">
                <template v-if="profile.currentAddressText || profile.currentAddressList">
                  {{ displayCurrentAddress }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 教育背景块 -->
        <el-card class="info-section">
          <template #header>
            <div class="section-header">
              <el-icon>
                <School/>
              </el-icon>
              <span>教育背景</span>
              <el-button type="primary" text @click="editSection('education', '教育背景')">编辑</el-button>
            </div>
          </template>
          <div class="timeline-container">
            <el-timeline>
              <el-timeline-item
                  v-for="(edu, index) in profile.sysPersonSchoolInfoList"
                  :key="index"
                  :timestamp="edu.graduationTime"
                  placement="top"
              >
                <el-card>
                  <h4>
                    <template v-if="edu.school">
                      {{ edu.school }}
                    </template>
                    <template v-else>
                      <el-icon><Warning /></el-icon> 未填写学校
                    </template>
                  </h4>
                  <p>
                    <template v-if="displaySchoolLevel(edu.schoolLevel)">
                      {{ displaySchoolLevel(edu.schoolLevel) }}
                    </template>
                    <template v-else>
                      <el-icon><Warning /></el-icon> 未填写
                    </template>
                    ·
                    <template v-if="edu.major">
                      {{ edu.major }}
                    </template>
                    <template v-else>
                      <el-icon><Warning /></el-icon> 未填写专业
                    </template>
                  </p>
                </el-card>
              </el-timeline-item>
            </el-timeline>
            <div v-if="!profile.sysPersonSchoolInfoList || profile.sysPersonSchoolInfoList.length === 0" class="empty-timeline">
              <el-icon><Warning /></el-icon> 暂无教育经历
            </div>
          </div>
          <div class="info-grid">
            <div class="info-item">
              <label>技能证书</label>
              <div class="info-value" :class="{'empty-data': !profile.certificateList || profile.certificateList.length === 0}">
                <el-tag
                    v-for="(cert, index) in profile.certificateList"
                    :key="index"
                    type="success"
                    class="cert-tag"
                >
                  {{ displayCertificate(cert) }}
                </el-tag>
                <span v-if="!profile.certificateList || profile.certificateList.length === 0">
                  <el-icon><Warning /></el-icon> 未填写
                </span>
              </div>
            </div>
            <div class="info-item full-width">
              <label>校园经历</label>
              <div class="info-value" :class="{'empty-data': !profile.campusExperience}">
                <template v-if="profile.campusExperience">
                  {{ profile.campusExperience }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 职业背景块 -->
        <el-card class="info-section">
          <template #header>
            <div class="section-header">
              <el-icon>
                <Briefcase/>
              </el-icon>
              <span>职业背景</span>
              <el-button type="primary" text @click="editSection('career', '职业背景')">编辑</el-button>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <label>职业名称</label>
              <div class="info-value" :class="{'empty-data': !profile.jobTitle}">
                <template v-if="profile.jobTitle">
                  {{ profile.jobTitle }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>职业类型</label>
              <div class="info-value" :class="{'empty-data': !profile.jobType}">
                <template v-if="displayJobType">
                  {{ displayJobType }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>公司名称</label>
              <div class="info-value" :class="{'empty-data': !profile.company}">
                <template v-if="profile.company">
                  {{ profile.company }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>工作开始时间</label>
              <div class="info-value" :class="{'empty-data': !profile.jobStartDate}">
                <template v-if="profile.jobStartDate">
                  {{ profile.jobStartDate }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>工龄（年）</label>
              <div class="info-value" :class="{'empty-data': !profile.workYears}">
                <template v-if="profile.workYears">
                  {{ profile.workYears }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>工资(元/月)</label>
              <div class="info-value" :class="{'empty-data': !profile.salary}">
                <template v-if="profile.salary">
                  {{ profile.salary.toLocaleString() }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>职位</label>
              <div class="info-value" :class="{'empty-data': !profile.position}">
                <template v-if="profile.position">
                  {{ profile.position }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 其它信息块 -->
        <el-card class="info-section">
          <template #header>
            <div class="section-header">
              <el-icon>
                <Collection/>
              </el-icon>
              <span>其它信息</span>
              <el-button type="primary" text @click="editSection('other', '其他')">编辑</el-button>
            </div>
          </template>
          <div class="info-grid">
            <div class="info-item">
              <label>银行账户</label>
              <div class="info-value" :class="{'empty-data': !profile.bankAccount}">
                <template v-if="profile.bankAccount">
                  **** **** **** {{ profile.bankAccount.slice(-4) }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>存款(元)</label>
              <div class="info-value" :class="{'empty-data': !profile.deposit}">
                <template v-if="profile.deposit">
                  {{ profile.deposit.toLocaleString() }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>是否有驾驶证</label>
              <div class="info-value" :class="{'empty-data': profile.hasDriverLicense === null}">
                <template v-if="profile.hasDriverLicense !== null">
                  {{ profile.hasDriverLicense == 1 ? '有' : '无' }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>血型</label>
              <div class="info-value" :class="{'empty-data': !profile.bloodType}">
                <template v-if="displayBloodType">
                  {{ displayBloodType }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>过敏史</label>
              <div class="info-value" :class="{'empty-data': !profile.allergyHistory}">
                <template v-if="profile.allergyHistory">
                  {{ profile.allergyHistory }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>慢性疾病</label>
              <div class="info-value" :class="{'empty-data': !profile.chronicDiseases}">
                <template v-if="profile.chronicDiseases">
                  {{ profile.chronicDiseases }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
            <div class="info-item">
              <label>兴趣爱好</label>
              <div class="info-value" :class="{'empty-data': !profile.hobbyList || profile.hobbyList.length === 0}">
                <el-tag
                    v-for="(hobby, index) in profile.hobbyList"
                    :key="index"
                    type="info"
                    class="hobby-tag"
                >
                  {{ displayHobby(hobby) }}
                </el-tag>
                <span v-if="!profile.hobbyList || profile.hobbyList.length === 0">
                  <el-icon><Warning /></el-icon> 未填写
                </span>
              </div>
            </div>
            <div class="info-item">
              <label>语言能力</label>
              <div class="info-value" :class="{'empty-data': !profile.languageList || profile.languageList.length === 0}">
                <el-tag
                    v-for="(lang, index) in profile.languageList"
                    :key="index"
                    class="lang-tag"
                >
                  {{ displayLanguage(lang) }}
                </el-tag>
                <span v-if="!profile.languageList || profile.languageList.length === 0">
                  <el-icon><Warning /></el-icon> 未填写
                </span>
              </div>
            </div>
            <div class="info-item full-width">
              <label>个人声明</label>
              <div class="info-value" :class="{'empty-data': !profile.personalStatement}">
                <template v-if="profile.personalStatement">
                  {{ profile.personalStatement }}
                </template>
                <template v-else>
                  <el-icon><Warning /></el-icon> 未填写
                </template>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 编辑模态框 -->
    <el-dialog
        v-model="editDialogVisible"
        :title="`编辑${currentEditName}信息`"
        width="50%"
    >
      <!-- 这里放置编辑表单 -->
      <div v-if="currentEditSection === 'basic'">
        <!-- 基础信息编辑表单 -->
        <el-form label-width="140px">
          <!--     第一行     -->
          <el-row>
            <el-col :span="12">
              <el-form-item label="主人姓名">
                <el-input v-model="profile.name" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="出生日期">
                <el-date-picker
                    v-model="profile.birthDate"
                    type="date"
                    style="width: 100%"
                    placeholder="选择日期时间"
                    :editable="false"
                    :value-format="'YYYY-MM-DD'"
                ></el-date-picker>
              </el-form-item>
            </el-col>
          </el-row>
          <!--     第二行     -->
          <el-row>
            <el-col :span="24">
              <el-form-item label="性别">
                <el-radio-group v-model="profile.gender">
                  <el-radio
                      v-for="(item, index) in sexItem"
                      :key="index"
                      :label="item.value"
                  >
                    {{ item.text }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
          </el-row>
          <!--     第三行      -->
          <el-row>
            <el-col :span="24">
              <el-form-item label="头像">
                <el-upload
                    class="avatar-uploader"
                    action="http://localhost:8400/superBrain/system/fileUpload"
                    :show-file-list="false"
                    :on-success="handleAvatarSuccess"
                    :headers="headers"
                >
                  <img v-if="profile.personPicture" :src="profile.personPicture" class="avatar" />
                  <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                </el-upload>
              </el-form-item>
            </el-col>
          </el-row>
          <!--     第四行      -->
          <!--     第五行     -->
          <el-row>
            <el-col :span="12">
              <el-form-item label="身高（cm）">
                <el-input-number v-model="profile.height" style="width: 100%" :precision="2" :step="0.1" :max="400"></el-input-number>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="体重（kg）">
                <el-input-number v-model="profile.weight" style="width: 100%" :precision="2" :step="0.1" :max="400"></el-input-number>
              </el-form-item>
            </el-col>
          </el-row>
          <!--    第六行     -->
          <el-row>
            <el-col :span="12">
              <el-form-item label="身份证号">
                <el-input v-model="profile.idNumber" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="民族">
                <el-select
                    v-model="profile.ethnicity"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                >
                  <el-option
                      v-for="item in ethnicityItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <!--     第七行     -->
          <el-row>
            <el-col :span="12">
              <el-form-item label="婚姻状况">
                <el-select
                    v-model="profile.maritalStatus"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                >
                  <el-option
                      v-for="item in maritalStatusItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="政治面貌">
                <el-select
                    v-model="profile.politicalStatus"
                    placeholder="请选择"
                    style="width: 100%"
                    clearable
                >
                  <el-option
                      v-for="item in politicalStatusItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>
      <!-- 其他区块的编辑表单 -->
      <!-- 联系方式编辑表单 -->
      <div v-else-if="currentEditSection === 'contact'">
        <el-form label-width="140px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="手机号码">
                <el-input v-model="profile.phone" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电子邮箱">
                <el-input v-model="profile.email" clearable />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="微信号">
                <el-input v-model="profile.wechat" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="抖音号">
                <el-input v-model="profile.douyin" clearable />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="籍贯地址">
                <el-cascader v-model="profile.hometownAddressList"
                             :options="formattedAddressOptions"
                             clearable
                             placeholder="请选择籍贯地址"
                             style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row v-if="profile.hometownAddressList != null">
            <el-col :span="24">
              <el-form-item label="">
                <el-input
                    v-model="profile.hometownAddressText"
                    type="textarea"
                    :rows="2"
                    placeholder="请补充详细地址"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="现居住地址">
                <el-cascader
                    v-model="profile.currentAddressList"
                    :options="formattedAddressOptions"
                    clearable
                    placeholder="请选择现居住地址"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row v-if="profile.currentAddressList != null">
            <el-col :span="24">
              <el-form-item label="">
                <el-input
                    v-model="profile.currentAddressText"
                    type="textarea"
                    :rows="2"
                    placeholder="请补充详细地址"
                    style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 教育背景编辑表单 -->
      <div v-else-if="currentEditSection === 'education'">
        <el-form label-width="140px">
          <el-row>
            <el-col :span="24">
              <el-form-item label="教育经历">
                <el-button type="primary" @click="addEducation">添加教育经历</el-button>
              </el-form-item>
            </el-col>
          </el-row>

          <div v-for="(edu, index) in profile.sysPersonSchoolInfoList" :key="index" class="education-item">
            <el-divider>教育经历 {{ index + 1 }}</el-divider>
            <el-row>
              <el-col :span="12">
                <el-form-item label="教育阶段">
                  <el-select
                      v-model="edu.schoolLevel"
                      placeholder="请选择"
                      style="width: 100%"
                      clearable
                  >
                    <el-option
                        v-for="item in schoolLevelItem"
                        :key="item.value"
                        :label="item.text"
                        :value="item.value"
                    ></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="毕业时间">
                  <el-date-picker
                      v-model="edu.graduationTime"
                      type="month"
                      style="width: 100%"
                      placeholder="选择年月"
                      value-format="YYYY-MM"
                  />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row>
              <el-col :span="12">
                <el-form-item label="学校名称">
                  <el-input v-model="edu.school" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="专业">
                  <el-input v-model="edu.major" clearable />
                </el-form-item>
              </el-col>
            </el-row>

            <el-row>
              <el-col :span="24">
                <el-button type="danger" @click="removeEducation(index)">删除此经历</el-button>
              </el-col>
            </el-row>
          </div>

          <el-row>
            <el-col :span="24">
              <el-form-item label="技能证书">
                <el-select
                    v-model="profile.certificateList"
                    multiple
                    filterable
                    clearable
                    style="width: 100%"
                    placeholder="请添加技能证书"
                >
                  <el-option
                      v-for="item in certificateOptions"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="校园经历">
                <el-input
                    v-model="profile.campusExperience"
                    type="textarea"
                    :rows="4"
                    placeholder="请描述您的校园经历"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 职业背景编辑表单 -->
      <div v-else-if="currentEditSection === 'career'">
        <el-form label-width="140px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="职业名称">
                <el-input v-model="profile.jobTitle" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职业类型">
                <el-select
                    v-model="profile.jobType"
                    clearable
                    style="width: 100%"
                    placeholder="请选择职业类型"
                >
                  <el-option
                      v-for="item in jobTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="公司名称">
                <el-input v-model="profile.company" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="职位">
                <el-input v-model="profile.position" clearable />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="工作开始时间">
                <el-date-picker
                    v-model="profile.jobStartDate"
                    type="date"
                    style="width: 100%"
                    placeholder="选择日期"
                    value-format="YYYY-MM-DD"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="工龄（年）">
                <el-input-number
                    v-model="profile.workYears"
                    style="width: 100%"
                    :min="0"
                    :step="1"
                    :precision="0"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <el-form-item label="工资(元/月)">
                <el-input-number
                    v-model="profile.salary"
                    style="width: 100%"
                    :min="0"
                    :step="1000"
                    :precision="0"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- 其他信息编辑表单 -->
      <div v-else-if="currentEditSection === 'other'">
        <el-form label-width="140px">
          <el-row>
            <el-col :span="12">
              <el-form-item label="银行账户">
                <el-input v-model="profile.bankAccount" clearable />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="存款(元)">
                <el-input-number
                    v-model="profile.deposit"
                    style="width: 100%"
                    :min="0"
                    :step="1000"
                    :precision="2"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="12">
              <el-form-item label="是否有驾驶证">
                <el-radio-group v-model="profile.hasDriverLicense">
                  <el-radio :label="1">有</el-radio>
                  <el-radio :label="0">无</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="血型">
                <el-select
                    v-model="profile.bloodType"
                    clearable
                    style="width: 100%"
                    placeholder="请选择血型类型"
                >
                  <el-option
                      v-for="item in bloodTypeItem"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="过敏史">
                <el-input
                    v-model="profile.allergyHistory"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入过敏史，如无请填写'无'"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="慢性疾病">
                <el-input
                    v-model="profile.chronicDiseases"
                    type="textarea"
                    :rows="2"
                    placeholder="请输入慢性疾病，如无请填写'无'"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="兴趣爱好">
                <el-select
                    v-model="profile.hobbyList"
                    multiple
                    filterable
                    clearable
                    style="width: 100%"
                    placeholder="请添加兴趣爱好"
                >
                  <el-option
                      v-for="item in hobbyOptions"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="语言能力">
                <el-select
                    v-model="profile.languageList"
                    multiple
                    filterable
                    clearable
                    style="width: 100%"
                    placeholder="请添加语言能力"
                >
                  <el-option
                      v-for="item in languageOptions"
                      :key="item.value"
                      :label="item.text"
                      :value="item.value"
                  ></el-option>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="24">
              <el-form-item label="个人声明">
                <el-input
                    v-model="profile.personalStatement"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入个人声明"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit()">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {DeleteDictById, GetAdministrative, GetKeyAndValueByType, GetSysCodeByType} from "@/api/sysDict";
import {useApp} from "@/pinia/modules/app";
import {dayjs, ElMessageBox} from "element-plus";
import { ElMessage } from 'element-plus'
import {DeletePersonSchoolInfoById, EditPersonInfo, GetPersonInfo} from "@/api/sysPersonInfo";
import { Warning } from '@element-plus/icons-vue'
// 新增的导入
import { saveAs } from 'file-saver';
import * as docx from 'docx';

const router = useRouter()
const route = useRoute()
//-------------------------------------基本组件定义及数据获取模块------------------------
// 模拟用户资料数据
const profile = ref({
  personPicture: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
  name: '张三',
  gender: 1,
  birthDate: '2001-07-15',
  height: 170,
  weight: 72,
  idNumber: '110105199005150036',
  ethnicity: 1,
  maritalStatus: 1,
  politicalStatus: 3,
  phone: '13800138000',
  email: 'zhangsan@example.com',
  wechat: 'zhangsan123',
  douyin: 'zhangsan_douyin',
  hometownAddress: '',
  hometownAddressList: [],
  hometownAddressText: '北京市海淀区',
  currentAddress: '',
  currentAddressList: [],
  currentAddressText: '上海市浦东新区张江高科技园区',
  certificates: '',
  certificateList: ['教师资格证', 'PMP项目管理证书'],
  campusExperience: '曾任学生会主席，组织多次校园活动',
  jobTitle: '高级软件工程师',
  jobType: 'IT/互联网',
  company: '上海某科技有限公司',
  jobStartDate: '2015-07-01',
  workYears: '8年',
  salary: 25000,
  position: '技术总监',
  bankAccount: '6225880123456789',
  deposit: 500000,
  hasDriverLicense: 0,
  bloodType: 'A型',
  allergyHistory: '青霉素过敏',
  chronicDiseases: '无',
  hobbies: '',
  hobbyList: [],
  languages: '',
  languageList: [],
  personalStatement: '本人性格开朗，工作认真负责，具有良好的团队合作精神。',
  sysPersonSchoolInfoList: []
})

// 编辑相关状态
const editDialogVisible = ref(false)
const currentEditSection = ref('')
const currentEditName = ref('');
//基本下拉列表项加载
const sexItem = ref([]); //性别下拉列表
const ethnicityItem = ref([]); //民族下拉列表
const maritalStatusItem = ref([]); //婚姻状况下拉列表
const politicalStatusItem = ref([]); //政治面貌下拉列表

//获取性别下拉列表
const getSexItem = async () => {
  const { data } = await GetKeyAndValueByType('sex');
  sexItem.value = data;
}
//获取民族下拉列表
const getEthnicityItem = async () => {
  const { data } = await GetKeyAndValueByType('t_person_info_ethnicity');
  ethnicityItem.value = data;
}
//获取婚姻状况下拉列表
const getMaritalStatusItem = async () => {
  const { data } = await GetKeyAndValueByType('t_person_info_marital');
  maritalStatusItem.value = data;
}
//获取政治面貌下拉列表项
const getPoliticalStatusItem = async () => {
  const { data } = await GetKeyAndValueByType('t_person_info_political');
  politicalStatusItem.value = data;
}

//----------------------------------- 计算显示的中文值---------------------------------------------
//民族响应式
const displayEthnicity = computed(() => {
  const item = ethnicityItem.value.find(e => e.value === profile.value.ethnicity);
  return item ? item.text : '';
});
//婚姻状况响应式
const displayMaritalStatus = computed(() => {
  const item = maritalStatusItem.value.find(e => e.value === profile.value.maritalStatus);
  return item ? item.text : '';
});
//政治面貌响应式
const displayPoliticalStatus = computed(() => {
  const item = politicalStatusItem.value.find(e => e.value === profile.value.politicalStatus);
  return item ? item.text : '';
});
//性别响应式
const displayGender = computed(() => {
  const item = sexItem.value.find(e => e.value === profile.value.gender);
  return item ? item.text : '';
});
// 计算年龄的 computed 属性
const age = computed(() => {
  if (!profile.value.birthDate) return '';

  const birthDay = dayjs(profile.value.birthDate);
  const today = dayjs();
  return today.diff(birthDay, 'year') + '岁'; // 计算年份差
});

//-------------------------------------基本数据查询------------------------------------------------
onMounted(() => {
  //加载下拉列表项
  //---------------基础信息块--------------
  //加载性别下拉项
  getSexItem();
  //加载民族下拉项
  getEthnicityItem();
  //加载婚姻状况下拉项
  getMaritalStatusItem();
  //加载政治面貌下拉项
  getPoliticalStatusItem();
  //----------------联系方式块---------------
  getFormattedAddressOptions();
  //----------------教育背景块--------------
  getSchoolLevelItem();
  getCertificateItem();
  //----------------职业背景块--------------
  getJobTypeItem()
  //-----------------其他模块---------------
  getBloodTypeItem();
  getHobbyItem();
  getLanguageItem();

  //加载个人信息
  getCurrentPersonInfo();
})
//获取当前登录用户基本信息
const getCurrentPersonInfo = async () => {
  const {data} = await GetPersonInfo();
  if (data == null){
    profile.value = {};
    profile.value.sysPersonSchoolInfoList = [];
  }else {
    profile.value = data;
    profile.value.sysPersonSchoolInfoList = data.sysPersonSchoolInfoList;
  }
}

//-------------------------------------联系方式编辑逻辑块----------------------------------------
// 完整的省市区数据
const formattedAddressOptions = ref([])
//获取中国行政区划码值对
const getFormattedAddressOptions = async () => {
  const {data} = await GetAdministrative();
  formattedAddressOptions.value = data;
}
// 公共地址查找函数
const findAddressText = (options, codes) => {
  if (!codes || codes.length === 0) return null;

  let result = [];
  let currentOptions = options;

  for (let i = 0; i < codes.length; i++) {
    const code = codes[i];
    const found = currentOptions.find(item => item.value === code);
    if (found) {
      result.push(found.label);
      if (found.children && i < codes.length - 1) {
        currentOptions = found.children;
      }
    } else {
      break;
    }
  }

  return result.length > 0 ? result.join('/') : null;
};
// 计算组件-籍贯地址显示（拼接hometownAddressText）
const displayHometownAddress = computed(() => {
  const addressFromCode = findAddressText(formattedAddressOptions.value, profile.value.hometownAddressList);
  const additionalText = profile.value.hometownAddressText;

  // 如果两者都有值，用空格连接
  if (addressFromCode && additionalText) {
    return `${addressFromCode} ${additionalText}`;
  }
  // 如果只有编码地址
  else if (addressFromCode) {
    return addressFromCode;
  }
  // 如果只有附加文本
  else if (additionalText) {
    return additionalText;
  }
  // 都没有
  else {
    return '';
  }
});

// 计算组件-现居住地址显示（拼接currentAddressText）
const displayCurrentAddress = computed(() => {
  const addressFromCode = findAddressText(formattedAddressOptions.value, profile.value.currentAddressList);
  const additionalText = profile.value.currentAddressText;

  // 如果两者都有值，用空格连接
  if (addressFromCode && additionalText) {
    return `${addressFromCode} ${additionalText}`;
  }
  // 如果只有编码地址
  else if (addressFromCode) {
    return addressFromCode;
  }
  // 如果只有附加文本
  else if (additionalText) {
    return additionalText;
  }
  // 都没有
  else {
    return '';
  }
});

//--------------------------------------教育背景编辑逻辑块---------------------------------------
// 教育背景操作
const addEducation = () => {
  profile.value.sysPersonSchoolInfoList.push({
    schoolLevel: '',
    school: '',
    graduationTime: '',
    major: ''
  })
}
const removeEducation = (index) => {
  ElMessageBox.confirm('此操作将永久移除该记录, 是否继续?', 'Warning', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    const { code, message } = await DeletePersonSchoolInfoById(profile.value.sysPersonSchoolInfoList[index].id)
    if (code === 200) {
      editDialogVisible.value = false
      ElMessage.success(message);
      getCurrentPersonInfo();
    } else {
      ElMessage.error(message)
    }
  })
  profile.value.sysPersonSchoolInfoList.splice(index, 1)
}
// 选项数据
const certificateOptions = ref([]) //技能证书下拉列表选项
const schoolLevelItem = ref([]) //学校等级下拉列表项

//获取学校等级下拉列表项
const getSchoolLevelItem = async () => {
  const { data } = await GetKeyAndValueByType('t_person_info_school_level');
  schoolLevelItem.value = data;
}
// 计算教育等级显示文本
const displaySchoolLevel = (level) => {
  const item = schoolLevelItem.value.find(e => e.value === level);
  return item ? item.text : '';
};
//获取技能证书下拉列表项
const getCertificateItem = async () => {
  const {data} = await GetSysCodeByType('t_person_info_certificate');
  certificateOptions.value = data;
}
// 计算证书的中文显示值
const displayCertificate = (certValue) => {
  const item = certificateOptions.value.find(e => e.value === certValue);
  return item ? item.text : certValue || '';
};

//--------------------------------------职业背景编辑块-------------------------------------------
const jobTypeItem = ref([]) //职业类型下拉选项
//获取职业类型
const getJobTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_person_info_job_type');
  jobTypeItem.value = data;
}
//计算职业类型显示文本
const displayJobType = computed(() => {
  const item = jobTypeItem.value.find(e => e.value === profile.value.jobType);
  return item ? item.text : '';
});

//---------------------------------------其他信息编辑块-----------------------------------------------
const bloodTypeItem = ref([]); //血型下拉选项
const hobbyOptions = ref([]) //兴趣爱好下拉项
const languageOptions = ref(['中文(母语)', '英语(流利)', '日语(基础)', '韩语(基础)', '法语(基础)'])
//获取血型类型
const getBloodTypeItem = async () => {
  const { data } = await GetKeyAndValueByType('t_person_info_blood_type');
  bloodTypeItem.value = data;
}
//计算血型类型
const displayBloodType = computed(() => {
  const item = bloodTypeItem.value.find(e => e.value === profile.value.bloodType);
  return item ? item.text : '';
});
//获取兴趣爱好下拉列表选项
const getHobbyItem = async () => {
  const {data} = await GetSysCodeByType('t_person_info_hobby');
  hobbyOptions.value = data;
}
//计算兴趣爱好下拉列表选项
const displayHobby = (certValue) => {
  const item = hobbyOptions.value.find(e => e.value === certValue);
  return item ? item.text : certValue || '';
};
//获取语言能力下拉列表选项
const getLanguageItem = async () => {
  const {data} = await GetSysCodeByType('t_person_info_language');
  languageOptions.value = data;
}
//计算语言能力下拉列表选项
const displayLanguage = (certValue) => {
  const item = languageOptions.value.find(e => e.value === certValue);
  return item ? item.text : certValue || '';
};

//--------------------------------------业务按钮操作---------------------------------------------
// 返回上一页
const goBack = () => {
  router.push({path: route.query.redirect || '/', replace: true})
}

// 编辑区块
const editSection = (section, titleName) => {
  currentEditSection.value = section;
  currentEditName.value = titleName;
  editDialogVisible.value = true;
}

// 保存编辑
const saveEdit = async () => {
  //打印
  //console.log("个人信息值",profile.value)
  //做校验
  if (profile.value.hometownAddressList == null || profile.value.hometownAddressList.length == 0){
    profile.value.hometownAddress = "";
    profile.value.hometownAddressText = "";
  }
  if (profile.value.currentAddressList == null || profile.value.currentAddressList.length == 0){
    profile.value.currentAddress = "";
    profile.value.currentAddressText = "";
  }
  if (profile.value.certificateList == null || profile.value.certificateList.length == 0){
    profile.value.certificates = "";
  }
  if (profile.value.hobbyList == null || profile.value.hobbyList.length == 0){
    profile.value.hobbies = "";
  }
  if (profile.value.languageList == null || profile.value.languageList.length == 0){
    profile.value.languages = "";
  }
  // 这里添加保存逻辑
  const {code, message} = await EditPersonInfo(currentEditSection.value, profile.value);
  if (code === 200){
    editDialogVisible.value = false
    ElMessage.success(message);
    //window.location.reload(); //刷新整个页面
    getCurrentPersonInfo();
  }else {
    ElMessage.error(message);
  }
}

//-------------------------------------------------文件上传--------------------------------------------
const headers = {
  token: useApp().authorization.token, // 从pinia中获取token，在进行文件上传的时候将token设置到请求头中
}

// 图像上传成功以后的事件处理函数
const handleAvatarSuccess = (response, uploadFile) => {
  profile.value.personPicture = response.data
}

//--------------------------------------导出Word文档功能-----------------------------------------
// 将图片URL转换为Uint8Array
const getImageBuffer = async (url) => {
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`Failed to fetch image: ${response.statusText}`);
    }
    const arrayBuffer = await response.arrayBuffer();
    return new Uint8Array(arrayBuffer);
  } catch (error) {
    console.error('Error fetching image:', error);
    return null;
  }
};

// 导出为Word文档
const exportToWord = async () => {
  // 获取头像图片数据
  let avatarImage = null;
  if (profile.value.personPicture) {
    const imageBuffer = await getImageBuffer(profile.value.personPicture);
    if (imageBuffer) {
      avatarImage = {
        data: imageBuffer,
        width: 100, // 设置图片宽度
        height: 100, // 设置图片高度
      };
    }
  }

  // 创建文档
  const doc = new docx.Document({
    sections: [{
      properties: {},
      children: [
        // 标题
        new docx.Paragraph({
          text: "个人画像信息",
          heading: docx.HeadingLevel.HEADING_1,
          alignment: docx.AlignmentType.CENTER,
          spacing: {
            after: 200,
          },
        }),

        // 头像部分
        avatarImage ? new docx.Paragraph({
          children: [
            new docx.ImageRun({
              data: avatarImage.data,
              transformation: {
                width: avatarImage.width,
                height: avatarImage.height,
              },
            }),
          ],
          alignment: docx.AlignmentType.CENTER,
          spacing: {
            after: 200,
          },
        }) : new docx.Paragraph({
          text: "无头像",
          italics: true,
          alignment: docx.AlignmentType.CENTER,
        }),

        // 基础信息部分
        new docx.Paragraph({
          text: "1. 基础信息",
          heading: docx.HeadingLevel.HEADING_2,
          spacing: {
            before: 200,
            after: 100,
          },
        }),
        createTableFromBasicInfo(),

        // 联系方式部分
        new docx.Paragraph({
          text: "2. 联系方式",
          heading: docx.HeadingLevel.HEADING_2,
          spacing: {
            before: 200,
            after: 100,
          },
        }),
        createTableFromContactInfo(),

        // 教育背景部分
        new docx.Paragraph({
          text: "3. 教育背景",
          heading: docx.HeadingLevel.HEADING_2,
          spacing: {
            before: 200,
            after: 100,
          },
        }),
        ...createEducationContent(),

        // 职业背景部分
        new docx.Paragraph({
          text: "4. 职业背景",
          heading: docx.HeadingLevel.HEADING_2,
          spacing: {
            before: 200,
            after: 100,
          },
        }),
        createTableFromCareerInfo(),

        // 其他信息部分
        new docx.Paragraph({
          text: "5. 其他信息",
          heading: docx.HeadingLevel.HEADING_2,
          spacing: {
            before: 200,
            after: 100,
          },
        }),
        createTableFromOtherInfo(),

        // 个人声明
        new docx.Paragraph({
          text: "6. 个人声明",
          heading: docx.HeadingLevel.HEADING_2,
          spacing: {
            before: 200,
            after: 100,
          },
        }),
        new docx.Paragraph({
          children: [
            new docx.TextRun({
              text: profile.value.personalStatement || "未填写",
              size: 22,
            }),
          ],
          spacing: {
            line: 300,
          },
        }),
      ],
    }],
  });

  // 生成文档并下载
  docx.Packer.toBlob(doc).then((blob) => {
    const fileName = `${profile.value.name || '个人'}-画像信息-${new Date().toLocaleDateString()}.docx`;
    saveAs(blob, fileName);
    ElMessage.success('导出Word文档成功');
  });
};

// 创建基础信息表格
const createTableFromBasicInfo = () => {
  const rows = [
    createTableRow("主人姓名", profile.value.name || "未填写"),
    createTableRow("性别", displayGender.value || "未填写"),
    createTableRow("出生日期", profile.value.birthDate || "未填写"),
    createTableRow("年龄", age.value || "未填写"),
    createTableRow("身高（cm）", String(profile.value.height) || "未填写"),
    createTableRow("体重（kg）", String(profile.value.weight) || "未填写"),
    createTableRow("身份证号", profile.value.idNumber ? `**************${profile.value.idNumber.slice(-4)}` : "未填写"),
    createTableRow("民族", displayEthnicity.value || "未填写"),
    createTableRow("婚姻状况", displayMaritalStatus.value || "未填写"),
    createTableRow("政治面貌", displayPoliticalStatus.value || "未填写"),
  ];

  return new docx.Table({
    width: {
      size: 100,
      type: docx.WidthType.PERCENTAGE,
    },
    borders: docx.TableBorders.NONE,
    rows,
  });
};

// 创建联系方式表格
const createTableFromContactInfo = () => {
  const rows = [
    createTableRow("手机号码", profile.value.phone ? profile.value.phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2') : "未填写"),
    createTableRow("电子邮箱", profile.value.email || "未填写"),
    createTableRow("微信号", profile.value.wechat || "未填写"),
    createTableRow("抖音号", profile.value.douyin || "未填写"),
    createTableRow("籍贯地址", displayHometownAddress.value || "未填写"),
    createTableRow("现居住地址", displayCurrentAddress.value || "未填写"),
  ];

  return new docx.Table({
    width: {
      size: 100,
      type: docx.WidthType.PERCENTAGE,
    },
    borders: docx.TableBorders.NONE,
    rows,
  });
};

// 创建教育背景内容
const createEducationContent = () => {
  const content = [];

  // 教育经历
  if (profile.value.sysPersonSchoolInfoList && profile.value.sysPersonSchoolInfoList.length > 0) {
    profile.value.sysPersonSchoolInfoList.forEach((edu, index) => {
      content.push(
          new docx.Paragraph({
            text: `教育经历 ${index + 1}`,
            heading: docx.HeadingLevel.HEADING_3,
            spacing: {
              before: 100,
              after: 50,
            },
          }),
          new docx.Paragraph({
            children: [
              new docx.TextRun({
                text: `${edu.school || "未填写学校"}`,
                bold: true,
              }),
            ],
          }),
          new docx.Paragraph({
            children: [
              new docx.TextRun({
                text: `${displaySchoolLevel(edu.schoolLevel) || "未填写"} · ${edu.major || "未填写专业"}`,
              }),
            ],
          }),
          new docx.Paragraph({
            children: [
              new docx.TextRun({
                text: `毕业时间: ${edu.graduationTime || "未填写"}`,
              }),
            ],
            spacing: {
              after: 100,
            },
          })
      );
    });
  } else {
    content.push(
        new docx.Paragraph({
          text: "暂无教育经历",
          italics: true,
        })
    );
  }

  // 技能证书
  content.push(
      new docx.Paragraph({
        text: "技能证书",
        heading: docx.HeadingLevel.HEADING_3,
        spacing: {
          before: 100,
          after: 50,
        },
      })
  );

  if (profile.value.certificateList && profile.value.certificateList.length > 0) {
    profile.value.certificateList.forEach(cert => {
      content.push(
          new docx.Paragraph({
            children: [
              new docx.TextRun({
                text: `• ${displayCertificate(cert)}`,
              }),
            ],
          })
      );
    });
  } else {
    content.push(
        new docx.Paragraph({
          text: "未填写",
          italics: true,
        })
    );
  }

  // 校园经历
  content.push(
      new docx.Paragraph({
        text: "校园经历",
        heading: docx.HeadingLevel.HEADING_3,
        spacing: {
          before: 100,
          after: 50,
        },
      }),
      new docx.Paragraph({
        children: [
          new docx.TextRun({
            text: profile.value.campusExperience || "未填写",
          }),
        ],
        spacing: {
          after: 100,
        },
      })
  );

  return content;
};

// 创建职业背景表格
const createTableFromCareerInfo = () => {
  const rows = [
    createTableRow("职业名称", profile.value.jobTitle || "未填写"),
    createTableRow("职业类型", displayJobType.value || "未填写"),
    createTableRow("公司名称", profile.value.company || "未填写"),
    createTableRow("工作开始时间", profile.value.jobStartDate || "未填写"),
    createTableRow("工龄（年）", String(profile.value.workYears) || "未填写"),
    createTableRow("工资(元/月)", profile.value.salary ? profile.value.salary.toLocaleString() : "未填写"),
    createTableRow("职位", profile.value.position || "未填写"),
  ];

  return new docx.Table({
    width: {
      size: 100,
      type: docx.WidthType.PERCENTAGE,
    },
    borders: docx.TableBorders.NONE,
    rows,
  });
};

// 创建其他信息表格
const createTableFromOtherInfo = () => {
  const rows = [
    createTableRow("银行账户", profile.value.bankAccount ? `**** **** **** ${profile.value.bankAccount.slice(-4)}` : "未填写"),
    createTableRow("存款(元)", profile.value.deposit ? profile.value.deposit.toLocaleString() : "未填写"),
    createTableRow("是否有驾驶证", profile.value.hasDriverLicense !== null ? (profile.value.hasDriverLicense == 1 ? '有' : '无') : "未填写"),
    createTableRow("血型", displayBloodType.value || "未填写"),
    createTableRow("过敏史", profile.value.allergyHistory || "未填写"),
    createTableRow("慢性疾病", profile.value.chronicDiseases || "未填写"),
  ];

  // 兴趣爱好
  rows.push(
      new docx.TableRow({
        children: [
          new docx.TableCell({
            children: [new docx.Paragraph({ text: "兴趣爱好", bold: true })],
            width: {
              size: 20,
              type: docx.WidthType.PERCENTAGE,
            },
          }),
          new docx.TableCell({
            children: [
              new docx.Paragraph({
                text: profile.value.hobbyList && profile.value.hobbyList.length > 0
                    ? profile.value.hobbyList.map(hobby => displayHobby(hobby)).join("、")
                    : "未填写",
              }),
            ],
            width: {
              size: 80,
              type: docx.WidthType.PERCENTAGE,
            },
          }),
        ],
      })
  );

  // 语言能力
  rows.push(
      new docx.TableRow({
        children: [
          new docx.TableCell({
            children: [new docx.Paragraph({ text: "语言能力", bold: true })],
            width: {
              size: 20,
              type: docx.WidthType.PERCENTAGE,
            },
          }),
          new docx.TableCell({
            children: [
              new docx.Paragraph({
                text: profile.value.languageList && profile.value.languageList.length > 0
                    ? profile.value.languageList.map(lang => displayLanguage(lang)).join("、")
                    : "未填写",
              }),
            ],
            width: {
              size: 80,
              type: docx.WidthType.PERCENTAGE,
            },
          }),
        ],
      })
  );

  return new docx.Table({
    width: {
      size: 100,
      type: docx.WidthType.PERCENTAGE,
    },
    borders: docx.TableBorders.NONE,
    rows,
  });
};

// 创建表格行的辅助函数
const createTableRow = (label, value) => {
  return new docx.TableRow({
    children: [
      new docx.TableCell({
        children: [new docx.Paragraph({ text: label, bold: true })],
        width: {
          size: 20,
          type: docx.WidthType.PERCENTAGE,
        },
      }),
      new docx.TableCell({
        children: [new docx.Paragraph({ text: value })],
        width: {
          size: 80,
          type: docx.WidthType.PERCENTAGE,
        },
      }),
    ],
  });
};
</script>

<style scoped>
/* 添加教育背景项的样式 */
.education-item {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .el-dialog {
    width: 90% !important;
  }
}

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

/* 外层容器样式 */
.profile-container {
  position: relative;
  width: 100%;
  min-height: 100vh;
  overflow-x: hidden;
}

/* 动态背景图样式 */
.bg-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  overflow: hidden;
}

.bg-gif {
  width: 100%;
  height: 100%;
  object-fit: cover;
  image-rendering: optimizeQuality;
}

/* 内容区域 */
.profile-content {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  z-index: 1;
}

/* 头部样式 */
.profile-header {
  display: flex;
  align-items: center;
  margin-bottom: 30px;
  padding-top: 20px;
}

.profile-header .title {
  flex: 1;
  text-align: center;
  color: #fff;
  font-size: 28px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
  margin-left: -60px; /* 平衡按钮位置 */
}

/* 导出按钮样式 */
.profile-header .export-btn {
  margin-left: auto;
  margin-right: 10px;
}

/* 资料卡片容器 */
.profile-card {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  padding: 30px;
  margin-bottom: 40px;
}

/* 信息区块样式 */
.info-section {
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.85);
  border-radius: 10px;
  overflow: hidden;
}

.info-section:last-child {
  margin-bottom: 0;
}

.info-section :deep(.el-card__header) {
  background: rgba(64, 158, 255, 0.1);
  border-bottom: 1px solid rgba(64, 158, 255, 0.2);
  padding: 15px 20px;
}

/* 区块头部样式 */
.section-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  color: #2d3a4b;
}

.section-header .el-icon {
  margin-right: 10px;
  font-size: 20px;
}

.section-header .el-button {
  margin-left: auto;
}

/* 信息网格布局 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 15px;
}

.info-item.full-width {
  grid-column: 1 / -1;
}

.info-item label {
  display: block;
  font-weight: bold;
  color: #555;
  margin-bottom: 5px;
  font-size: 14px;
}

.info-item .info-value {
  min-height: 20px;
  padding: 8px 12px;
  background: rgba(245, 247, 250, 0.8);
  border-radius: 6px;
  word-break: break-word;
  transition: all 0.3s ease;
}

/* 已填写信息的样式 */
.info-item .info-value:not(.empty-data) {
  background: #f0f9eb;
  color: #67c23a;
  border-left: 3px solid #67c23a;
}

/* 未填写信息的样式 */
.info-item .info-value.empty-data {
  background: #fef0f0;
  color: #f56c6c;
  border-left: 3px solid #f56c6c;
}

.info-item .info-value.empty-data .el-icon {
  margin-right: 5px;
}

.avatar-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.empty-avatar {
  color: #f56c6c;
  font-size: 14px;
}

/* 标签样式 */
.cert-tag, .hobby-tag, .lang-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

/* 时间线样式 */
.timeline-container {
  padding: 15px;
}

.timeline-container :deep(.el-timeline-item__timestamp) {
  color: #666;
  font-size: 14px;
}

.timeline-container .el-card {
  margin-bottom: 10px;
  background: rgba(245, 247, 250, 0.8);
}

.timeline-container .el-card h4 {
  margin: 0 0 5px 0;
  color: #333;
}

.timeline-container .el-card p {
  margin: 0;
  color: #666;
  font-size: 13px;
}

.empty-timeline {
  text-align: center;
  padding: 20px;
  color: #f56c6c;
  background: #fef0f0;
  border-radius: 4px;
  margin-top: 15px;
}

.empty-timeline .el-icon {
  margin-right: 5px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .profile-content {
    padding: 10px;
  }

  /* 头部样式 */
  .profile-header {
    display: flex;
    justify-content: space-between; /* 两端对齐 */
    align-items: center;
    margin-bottom: 30px;
    padding-top: 20px;
    width: 100%; /* 确保宽度填满容器 */
  }

  .profile-header .title {
    /* 移除之前的 margin-left 调整 */
    color: #fff;
    font-size: 28px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
    /* 如果需要标题居中，可以添加以下属性 */
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
  }

  /* 左侧按钮容器 */
  .header-left {
    margin-right: auto; /* 左对齐 */
  }

  /* 右侧按钮容器 */
  .header-right {
    margin-left: auto; /* 右对齐 */
  }

  .profile-header .export-btn {
    margin: 10px 0 0 0;
    width: 100%;
  }

  .profile-card {
    padding: 15px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }

  .info-section :deep(.el-card__header) {
    padding: 12px 15px;
  }
}
</style>