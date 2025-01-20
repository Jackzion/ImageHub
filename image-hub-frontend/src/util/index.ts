import saveAs from 'file-saver'
export function downloadImage(url?:string,fileName?:string){
    if(!url) return
    saveAs(url,fileName)
}