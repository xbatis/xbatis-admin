<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { Lock, User } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../../stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);

const formState = reactive({
  username: 'admin',
  password: '123456'
});

const rules: FormRules<typeof formState> = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  loading.value = true;
  try {
    await authStore.signIn(formState);
    ElMessage.success('登录成功');
    router.push('/dashboard');
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '登录失败');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-screen">
    <div class="login-orbit login-orbit-left"></div>
    <div class="login-orbit login-orbit-right"></div>

    <section class="login-stage">
      <div class="login-story">
        <div class="login-story__head">
          <div class="login-story-mark">SX</div>
          <span class="login-story-badge">Access Matrix</span>
        </div>

        <p class="login-story-kicker">Element Plus Admin Experience</p>
        <h1>把用户、角色、菜单和按钮权限放进一套更清晰的控制台里。</h1>
        <p class="login-story-copy">
          当前系统围绕 RBAC 权限模型组织，前端路由、左侧导航、操作按钮和数据边界全部由同一套权限源驱动。
        </p>

        <div class="login-story-grid">
          <div>
            <strong>Unified ACL</strong>
            <span>路由与按钮权限共享一份配置</span>
          </div>
          <div>
            <strong>Spring + xbatis</strong>
            <span>后端接口与数据映射保持同一套结构语言</span>
          </div>
          <div>
            <strong>PostgreSQL</strong>
            <span>权限数据、用户状态与登录记录持久化</span>
          </div>
        </div>

        <div class="login-signal-strip">
          <div>
            <span>Menu-driven routes</span>
            <strong>Online</strong>
          </div>
          <div>
            <span>Button-level ACL</span>
            <strong>Ready</strong>
          </div>
          <div>
            <span>Session bootstrap</span>
            <strong>Stable</strong>
          </div>
        </div>
      </div>

      <div class="login-card">
        <div class="login-card-head">
          <span class="login-card-pill">Secure Access</span>
          <h2>登录控制台</h2>
          <p>使用默认管理员账户快速进入系统，查看最新的控制台风格。</p>
        </div>

        <el-form ref="formRef" :model="formState" :rules="rules" label-position="top" @keyup.enter="submit">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formState.username" size="large" placeholder="admin">
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <el-input v-model="formState.password" size="large" type="password" show-password placeholder="请输入密码">
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button type="primary" size="large" class="login-submit" :loading="loading" @click="submit">
            进入后台
          </el-button>
        </el-form>

        <div class="login-hint">
          <span>默认账号</span>
          <code>admin / 123456</code>
        </div>
      </div>
    </section>
  </div>
</template>
