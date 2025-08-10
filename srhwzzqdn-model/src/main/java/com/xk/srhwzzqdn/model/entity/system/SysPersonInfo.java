package com.xk.srhwzzqdn.model.entity.system;

import com.xk.srhwzzqdn.model.entity.base.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "个人信息实体")
public class SysPersonInfo extends BaseEntity {
    @Schema(description = "账号ID")
    private String accountId;

    @Schema(description = "真实姓名")
    private String name;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "出生日期")
    private String birthDate;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "身高(cm)")
    private BigDecimal height;

    @Schema(description = "体重(kg)")
    private BigDecimal weight;

    @Schema(description = "身份证号")
    private String idNumber;

    @Schema(description = "民族")
    private Integer ethnicity;

    @Schema(description = "婚姻状态")
    private Integer maritalStatus;

    @Schema(description = "政治面貌")
    private Integer politicalStatus;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "电子邮箱")
    private String email;

    @Schema(description = "微信号")
    private String wechat;

    @Schema(description = "抖音号")
    private String douyin;

    @Schema(description = "籍贯地址")
    private String hometownAddress;

    private List<String> hometownAddressList;

    @Schema(description = "当前居住地址")
    private String currentAddress;

    private List<String> currentAddressList;

    @Schema(description = "证书")
    private String certificates;

    private List<String> certificateList;

    @Schema(description = "校园经历")
    private String campusExperience;

    @Schema(description = "职业名称")
    private String jobTitle;

    @Schema(description = "职业类型")
    private Integer jobType;

    @Schema(description = "公司")
    private String company;

    @Schema(description = "工作开始日期")
    private String jobStartDate;

    @Schema(description = "工龄")
    private Integer workYears;

    @Schema(description = "工资")
    private Integer salary;

    @Schema(description = "职位")
    private String position;

    @Schema(description = "银行账户")
    private String bankAccount;

    @Schema(description = "存款")
    private Long deposit;

    @Schema(description = "是否有驾驶证(1是0否)")
    private Integer hasDriverLicense;

    @Schema(description = "血型")
    private Integer bloodType;

    @Schema(description = "过敏史")
    private String allergyHistory;

    @Schema(description = "慢性疾病")
    private String chronicDiseases;

    @Schema(description = "兴趣爱好")
    private String hobbies;

    private List<String> hobbyList;

    @Schema(description = "掌握语言")
    private String languages;

    private List<String> languageList;

    @Schema(description = "个人说明")
    private String personalStatement;

    @Schema(description = "个人照片")
    private String personPicture;

    @Schema(description = "操作块")
    private String codeBlock;

    private String hometownAddressText;
    private String currentAddressText;

    private List<SysPersonSchoolInfo> sysPersonSchoolInfoList;
}
