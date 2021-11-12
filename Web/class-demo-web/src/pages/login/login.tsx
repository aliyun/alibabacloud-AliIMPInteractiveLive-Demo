import styles from './login.less'
import { Form, Input, Button, Select, message } from 'antd'
import { useEffect } from 'react'
import { setPre } from '@/utils'

const { Option } = Select

interface LoginForm {
  classId?: string
  userId?: string
  nick?: string
  role: 'teacher' | 'student'
}

const classIdReg = /^[A-Za-z0-9-]+$/
const userReg = /^(\w)+$/

export default function Login(props: any) {
  const [form] = Form.useForm()
  const onFinish = async (val: LoginForm) => {
    if (!val.userId) return
    window.sessionStorage.setItem('userId', val.userId)
    window.sessionStorage.setItem('classId', val.classId || '')
    window.sessionStorage.setItem('role', val.role)
    props.history.replace('/doLogin')
  }
  useEffect(() => {
    window.sessionStorage.removeItem('user')
    window.sessionStorage.removeItem('classId')
    window.sessionStorage.removeItem('role')
  }, [])
  const isPre = window.localStorage.getItem('pre')
  return (
    <div className={styles['login-page']}>
      {isPre && <div className={styles.ispre}>预发</div>}
      <div className={styles['login-container']}>
        <div className={styles['logo']}></div>
        <div className={styles['login-form-container']}>
          <div className={styles['login-form-main']}>
            <div className={styles['title']} onClick={setPre}>
              阿里云互动课堂
            </div>
            <Form
              form={form}
              layout="vertical"
              className="standard-demo-login-form"
              onFinish={onFinish}
              initialValues={{
                classType: 'class',
                role: 'teacher',
              }}
            >
              <Form.Item
                label="请输入课堂号"
                name="classId"
                rules={[{ pattern: classIdReg, message: '请输入正确的课堂号' }]}
                tooltip="输入课堂号进入已有课堂，不输入将创建一个新课堂"
              >
                <Input />
              </Form.Item>
              <Form.Item
                label="请输入用户ID"
                name="userId"
                rules={[
                  { required: true, message: '请输入用户ID' },
                  { pattern: userReg, message: '只能输入字母和数字' },
                ]}
              >
                <Input />
              </Form.Item>
              <Form.Item label="请选择课堂类型" name="classType">
                <Select disabled>
                  <Option value="class">低延迟大班课</Option>
                </Select>
              </Form.Item>
              <Form.Item label="请选择课堂角色" name="role">
                <Select>
                  <Option value="teacher">教师</Option>
                  <Option value="student">学生</Option>
                </Select>
              </Form.Item>
              <Form.Item>
                <Button type="primary" block htmlType="submit">
                  进入课堂
                </Button>
              </Form.Item>
            </Form>
          </div>
        </div>
      </div>
    </div>
  )
}
