import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as XLSX from 'xlsx'
import { saveAs } from 'file-saver'

/**
 * 通用Excel导出Hook
 * @param {Object} options - 配置选项
 * @param {Array} options.availableColumns - 可导出的列配置 [{ key, label, width }]
 * @param {Function} options.fetchAllData - 获取全部数据的函数（可选）
 * @param {Function} options.dataFormatter - 数据格式化函数（可选）
 * @param {String} options.defaultFileName - 默认导出文件名
 * @param {String} options.sheetName - Excel工作表名称
 * @returns {Object} 导出相关状态和方法
 */
export function useExport(options) {
    const {
        availableColumns = [],
        fetchAllData = null,
        dataFormatter = null,
        defaultFileName = '导出数据',
        sheetName = '数据'
    } = options

    // 导出相关状态
    const exportDialogVisible = ref(false)
    const exportScope = ref('current') // 'current' 或 'all'
    const exportFileName = ref(defaultFileName)
    const exportLoading = ref(false)
    const selectedColumns = ref([])

    // 当前页数据（需要外部传入）
    const currentPageData = ref([])
    // 总数据量（需要外部传入）
    const totalCount = ref(0)

    // 计算选中的列配置
    const selectedColumnConfig = computed(() => {
        return availableColumns.filter(col => selectedColumns.value.includes(col.key))
    })

    // 初始化选中的列（默认全选）
    const initSelectedColumns = () => {
        selectedColumns.value = availableColumns.map(col => col.key)
    }

    // 显示导出对话框
    const showExportDialog = (pageData, total) => {
        currentPageData.value = pageData || []
        totalCount.value = total || 0

        if (currentPageData.value.length === 0 && exportScope.value === 'current') {
            ElMessage.warning('当前页没有数据可导出')
            return
        }
        if (totalCount.value === 0 && exportScope.value === 'all') {
            ElMessage.warning('没有数据可导出')
            return
        }
        exportDialogVisible.value = true
    }

    /**
     * 通用Excel导出函数
     * @param {Array} data - 要导出的数据
     * @param {Array} columns - 列配置
     * @param {Object} customOptions - 自定义选项
     * @returns {Object} workbook对象
     */
    const exportExcel = (data, columns, customOptions = {}) => {
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

                    // 处理对象
                    if (typeof value === 'object') {
                        return JSON.stringify(value)
                    }

                    return value
                })
                worksheetData.push(row)
            })

            // 创建工作表
            const worksheet = XLSX.utils.aoa_to_sheet(worksheetData)

            // 设置列宽
            worksheet['!cols'] = columns.map(col => ({ wch: col.width || 15 }))

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
                        font: { bold: true, color: { rgb: 'FFFFFF' } },
                        fill: { fgColor: { rgb: '409EFF' } },
                        alignment: { horizontal: 'center', vertical: 'center' },
                        border: {
                            top: { style: 'thin', color: { rgb: '000000' } },
                            left: { style: 'thin', color: { rgb: '000000' } },
                            bottom: { style: 'thin', color: { rgb: '000000' } },
                            right: { style: 'thin', color: { rgb: '000000' } },
                        },
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
                                top: { style: 'thin', color: { rgb: 'DDDDDD' } },
                                left: { style: 'thin', color: { rgb: 'DDDDDD' } },
                                bottom: { style: 'thin', color: { rgb: 'DDDDDD' } },
                                right: { style: 'thin', color: { rgb: 'DDDDDD' } },
                            },
                            alignment: {
                                horizontal: 'left',
                                vertical: 'center',
                                wrapText: true,
                            },
                        }

                        // 隔行变色
                        if (R % 2 === 0) {
                            worksheet[cellAddress].s.fill = { fgColor: { rgb: 'F8F9FA' } }
                        }
                    }
                }
            }

            // 将工作表添加到工作簿
            const finalSheetName = customOptions.sheetName || sheetName
            XLSX.utils.book_append_sheet(workbook, worksheet, finalSheetName)

            return workbook
        } catch (error) {
            console.error('创建Excel失败:', error)
            throw error
        }
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
                exportData = currentPageData.value
                dataCount = currentPageData.value.length
            } else {
                // 导出全部数据
                if (!fetchAllData) {
                    ElMessage.error('未提供获取全部数据的函数')
                    return
                }
                exportData = await fetchAllData()
                dataCount = exportData.length
            }

            if (exportData.length === 0) {
                ElMessage.warning('没有数据可导出')
                return
            }

            // 生成文件名
            const fileName = exportFileName.value || defaultFileName
            const timestamp = new Date().getTime()
            const fullFileName = `${fileName}_${timestamp}.xlsx`

            // 使用通用导出函数
            const workbook = exportExcel(
                exportData,
                selectedColumnConfig.value,
                { sheetName }
            )

            // 生成Excel文件并下载
            const excelBuffer = XLSX.write(workbook, {
                bookType: 'xlsx',
                type: 'array',
                cellStyles: true,
            })

            const blob = new Blob([excelBuffer], {
                type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
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

    // 重置状态
    const resetExport = () => {
        exportScope.value = 'current'
        exportFileName.value = defaultFileName
        exportLoading.value = false
        initSelectedColumns()
    }

    return {
        // 状态
        exportDialogVisible,
        exportScope,
        exportFileName,
        exportLoading,
        selectedColumns,
        availableColumns,
        selectedColumnConfig,

        // 方法
        initSelectedColumns,
        showExportDialog,
        handleExport,
        resetExport,
        exportExcel, // 底层导出函数，也可以直接使用
    }
}