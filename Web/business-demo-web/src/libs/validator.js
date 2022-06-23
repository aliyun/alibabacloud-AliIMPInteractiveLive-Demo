import Vue from 'vue'
import VeeValidate, { Validator } from 'vee-validate'
import zh_CN from 'vee-validate/dist/locale/zh_CN'

Validator.localize('zh_CN', zh_CN)
const config = {
  errorBagName: 'errorsBags',
  fieldsBagName: 'fieldBags'
}

const validators = {
  mobile: {
    getMessage: field => '填写内容不是个手机号',
    validate: value => /^1[3-9]\d{9}$/g.test(value)
  }
}

Object.keys(validators).forEach(item => {
  Validator.extend(item, validators[item])
})

Vue.use(VeeValidate, config)
