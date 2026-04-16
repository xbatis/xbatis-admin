<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { PageResponse } from '../../types/common';
import type { OptionItem, UserFormPayload, UserItem } from '../../types/system';
import { createUser, getRoleOptions, getUsers, resetUserPassword, updateUser, updateUserStatus } from '../../api/system';
import { useAuthStore } from '../../stores/auth';

const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const loading = ref(false);
const saving = ref(false);
const modalOpen = ref(false);
const editingId = ref<number | null>(null);
const roleOptions = ref<OptionItem[]>([]);

const query = reactive({
  current: 1,
  pageSize: 10,
  keyword: '',
  enabled: undefined as number | undefined
});

const pageData = ref<PageResponse<UserItem>>({
  records: [],
  current: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0
});

const formModel = reactive<UserFormPayload>({
  username: '',
  displayName: '',
  email: '',
  phone: '',
  enabled: 1,
  remark: '',
  roleIds: []
});

function validateEmail(_rule: unknown, value: string | undefined, callback: (error?: Error) => void) {
  const text = value?.trim();
  if (!text || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(text)) {
    callback();
    return;
  }
  callback(new Error('请输入正确的邮箱地址'));
}

function validatePhone(_rule: unknown, value: string | undefined, callback: (error?: Error) => void) {
  const text = value?.trim();
  if (!text || /^1[3-9]\d{9}$/.test(text)) {
    callback();
    return;
  }
  callback(new Error('请输入正确的 11 位手机号'));
}

const rules: FormRules<UserFormPayload> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度需为 3-32 位', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_.@-]+$/, message: '用户名只能包含字母、数字、下划线、点、横线和 @', trigger: 'blur' }
  ],
  displayName: [
    { required: true, message: '请输入显示名', trigger: 'blur' },
    { min: 2, max: 32, message: '显示名长度需为 2-32 位', trigger: 'blur' }
  ],
  email: [{ validator: validateEmail, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  roleIds: [{ required: true, type: 'array', min: 1, message: '至少选择一个角色', trigger: 'change' }],
  remark: [{ max: 200, message: '备注不能超过 200 个字符', trigger: 'blur' }]
};

const canCreate = computed(() => authStore.hasPermission('sys:user:create'));
const canEdit = computed(() => authStore.hasPermission('sys:user:update'));
const canStatus = computed(() => authStore.hasPermission('sys:user:status'));
const canReset = computed(() => authStore.hasPermission('sys:user:reset-password'));

const summary = computed(() => {
  const enabledCount = pageData.value.records.filter((item) => item.enabled === 1).length;
  const disabledCount = pageData.value.records.filter((item) => item.enabled !== 1).length;
  const roleVariety = new Set(pageData.value.records.flatMap((item) => item.roleNames)).size;
  return [
    { label: '当前分页用户', value: pageData.value.records.length },
    { label: '启用中', value: enabledCount },
    { label: '禁用中', value: disabledCount },
    { label: '角色种类', value: roleVariety }
  ];
});

function resetFormModel() {
  Object.assign(formModel, {
    username: '',
    displayName: '',
    email: '',
    phone: '',
    enabled: 1,
    remark: '',
    roleIds: []
  });
}

async function clearValidation() {
  await nextTick();
  formRef.value?.clearValidate();
}

async function loadData() {
  loading.value = true;
  try {
    pageData.value = await getUsers(query);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载用户失败');
  } finally {
    loading.value = false;
  }
}

async function loadRoleData() {
  try {
    roleOptions.value = await getRoleOptions();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载角色选项失败');
  }
}

async function openCreate() {
  editingId.value = null;
  resetFormModel();
  modalOpen.value = true;
  await clearValidation();
}

async function openEdit(record: UserItem) {
  editingId.value = record.id;
  Object.assign(formModel, {
    username: record.username,
    displayName: record.displayName,
    email: record.email || '',
    phone: record.phone || '',
    enabled: record.enabled,
    remark: record.remark || '',
    roleIds: [...record.roleIds]
  });
  modalOpen.value = true;
  await clearValidation();
}

function closeDialog() {
  modalOpen.value = false;
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  saving.value = true;
  try {
    const payload: UserFormPayload = {
      ...formModel,
      username: formModel.username.trim(),
      displayName: formModel.displayName.trim(),
      email: formModel.email?.trim(),
      phone: formModel.phone?.trim(),
      remark: formModel.remark?.trim()
    };
    if (editingId.value) {
      await updateUser(editingId.value, payload);
      ElMessage.success('用户更新成功');
    } else {
      await createUser(payload);
      ElMessage.success('用户创建成功，默认密码为 123456');
    }
    modalOpen.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleStatusChange(record: UserItem, checked: string | number | boolean) {
  try {
    await updateUserStatus(record.id, checked ? 1 : 0);
    ElMessage.success('状态已更新');
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '状态更新失败');
  }
}

async function handleResetPassword(record: UserItem) {
  try {
    await ElMessageBox.confirm(`确认将 ${record.displayName} 的密码重置为默认值？`, '确认操作', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    });
    await resetUserPassword(record.id);
    ElMessage.success('密码已重置为 123456');
  } catch {
    // noop
  }
}

async function handleSearch() {
  query.current = 1;
  await loadData();
}

async function resetSearch() {
  query.keyword = '';
  query.enabled = undefined;
  query.current = 1;
  await loadData();
}

async function handlePageChange(page: number) {
  query.current = page;
  await loadData();
}

async function handlePageSizeChange(pageSize: number) {
  query.pageSize = pageSize;
  query.current = 1;
  await loadData();
}

onMounted(async () => {
  await loadRoleData();
  await loadData();
});
</script>

<template>
  <div class="page-stack">
    <section class="section-banner">
      <div>
        <p class="section-kicker">System Users</p>
        <h3>用户管理</h3>
        <p class="section-banner__copy">维护登录身份、启停状态和角色归属，用户视图直接反映当前权限模型的分配结果。</p>
      </div>
      <div class="section-banner__stats">
        <div v-for="item in summary" :key="item.label" class="mini-stat">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </section>

    <section class="surface-panel">
      <div class="section-head">
        <div>
          <p class="section-kicker">Directory</p>
          <h3>用户目录</h3>
        </div>
        <el-button v-if="canCreate" type="primary" @click="openCreate">新增用户</el-button>
      </div>

      <div class="control-bar">
        <div class="query-bar">
          <el-input v-model="query.keyword" clearable placeholder="用户名搜索" class="query-input" @keyup.enter="handleSearch" />
          <el-select v-model="query.enabled" clearable placeholder="状态" class="query-select">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </div>
        <div class="query-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" row-key="id" :data="pageData.records" class="data-table">
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="displayName" label="显示名" min-width="140" />
        <el-table-column label="角色" min-width="180">
          <template #default="{ row }">
            <div class="table-tags">
              <el-tag v-for="role in row.roleNames" :key="role" effect="plain" type="primary">{{ role }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-switch
              :model-value="row.enabled === 1"
              :disabled="!canStatus"
              inline-prompt
              active-text="启"
              inactive-text="禁"
              @change="(checked) => handleStatusChange(row, checked)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginAt" label="最后登录" min-width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="canReset" link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-foot">
        <el-pagination
          v-model:current-page="query.current"
          v-model:page-size="query.pageSize"
          layout="total, sizes, prev, pager, next"
          :total="pageData.total"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
    </section>

    <el-dialog v-model="modalOpen" :title="editingId ? '编辑用户' : '新增用户'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="formModel" :rules="rules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formModel.username" placeholder="例如：admin" />
        </el-form-item>
        <el-form-item label="显示名" prop="displayName">
          <el-input v-model="formModel.displayName" placeholder="例如：系统管理员" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="formModel.email" placeholder="可选，如 admin@example.com" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formModel.phone" placeholder="可选，11 位手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="formModel.roleIds" multiple collapse-tags collapse-tags-tooltip placeholder="请选择角色">
            <el-option v-for="role in roleOptions" :key="role.value" :label="role.label" :value="role.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formModel.enabled">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formModel.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
