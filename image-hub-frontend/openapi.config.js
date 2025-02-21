import { generateService } from '@umijs/openapi'

generateService({
  requestLibPath: "import request from '@/axioss/request'",
  schemaPath: 'http://localhost:8808/api/v2/api-docs',
  serversPath: './src',
})