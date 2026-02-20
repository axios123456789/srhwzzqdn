//获取当天时间范围
export function getTodayTimeRange() {
  const now = new Date()
  const beginTime = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
    0,
    0,
    0
  )
  const endTime = new Date(
    now.getFullYear(),
    now.getMonth(),
    now.getDate(),
    23,
    59,
    59
  )
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
  return [beginTimeStr, endTimeStr]
}

// 通用方法：根据值和映射表获取中文文本
export function getDisplayText(value, mappingArray) {
  if (!value) return '-'
  const foundItem = mappingArray.find(item => item.value === value)
  return foundItem ? foundItem.text : value
}

//通用方法：根据值和映射表获取中文文本值（树形）
export function getDisplayTextByTree(value, treeData){
  if (!value) return '-'

  const traverse = nodes => {
    for (const node of nodes) {
      if (node.value === value) {
        return node.text
      }
      if (node.children?.length) {
        const result = traverse(node.children)
        if (result) return result
      }
    }
    return null
  }

  const result = traverse(treeData)
  return result || value
}

// 获取文档名称列表，多个文档用逗号分隔
export function getDocumentNames(documentPaths) {
  if (!documentPaths || documentPaths.length === 0) return '-'

  // 如果是字符串，分割成数组
  const paths = Array.isArray(documentPaths)
      ? documentPaths
      : documentPaths.split(',')

  // 从每个路径中提取文件名并清洗
  return paths
      .map(path => {
        // 从路径中提取文件名部分
        const fileName = path.substring(path.lastIndexOf('/') + 1)
        // 使用现有的cleanFileName函数清洗文件名
        return cleanFileName(fileName)
      })
      .join(', ')
}
// 文件名清洗函数 - 支持大小写的32位十六进制UUID
export function cleanFileName(fileName) {
  if (!fileName) return '未知文件'

  // 方法1: 移除32位十六进制UUID前缀（支持大小写）
  const hex32Pattern = /^[a-fA-F0-9]{32}/
  if (hex32Pattern.test(fileName)) {
    return fileName.substring(32)
  }

  // 方法2: 移除下划线分隔的UUID
  const underscoreIndex = fileName.indexOf('_')
  if (underscoreIndex > 0) {
    return fileName.substring(underscoreIndex + 1)
  }

  // 方法3: 移除8位数字时间戳前缀
  const timestampPattern = /^\d{8}(_)?/
  if (timestampPattern.test(fileName)) {
    const match = fileName.match(timestampPattern)
    if (match) {
      return fileName.substring(match[0].length)
    }
  }

  // 方法4: 移除32位UUID（带连字符格式）如 xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
  const uuidWithHyphenPattern = /^[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}/
  if (uuidWithHyphenPattern.test(fileName)) {
    return fileName.substring(36)
  }

  // 方法5: 移除开头的任意32位十六进制字符（不区分大小写）
  const hex32AnyPattern = /^[a-fA-F0-9]{32}[._-]?/
  if (hex32AnyPattern.test(fileName)) {
    return fileName.replace(hex32AnyPattern, '')
  }

  return fileName
}
