import saveAs from 'file-saver'
export function downloadImage(url?:string,fileName?:string){
    if(!url) return
    saveAs(url,fileName)
}

export function toHexColor(input:string){
    // 去掉 0x
    const colorValue = input.startsWith('0x') ? input.slice(2) : input

    // 转16进制,再转为6位字符串
    const hexColor = parseInt(colorValue, 16).toString(16).padStart(6, '0')

    // 返回标准 #RRGGBB 格式
    return `#${hexColor}`
}