import { UA } from '@/libs/utils'

export const downloadPCMixin = {
  data: () => ({
    UA: UA
  }),

  methods: {
    downloadPC() {
      const url = UA.isMac ? 'https://help.aliyun.com/document_detail/415794.html' : 'https://help.aliyun.com/document_detail/409690.html'
      window.open(url)
    }
  }
}
