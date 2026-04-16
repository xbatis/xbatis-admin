<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import type { FormInstance, FormRules, TreeInstance } from 'element-plus';
import type { PageResponse } from '../../types/common';
import type { MenuNode, RoleFormPayload, RoleItem } from '../../types/system';
import { createRole, deleteRole, getMenus, getRoleDetail, getRoles, updateRole } from '../../api/system';
import { useAuthStore } from '../../stores/auth';
import { toTreeSelectData } from '../../utils/tree';

const authStore = useAuthStore();
const formRef = ref<FormInstance>();
const treeRef = ref<TreeInstance>();
const loading = ref(false);
const saving = ref(false);
const modalOpen = ref(false);
const editingId = ref<number | null>(null);
const menuTree = ref<MenuNode[]>([]);

const query = reactive({
  current: 1,
  pageSize: 10,
  keyword: '',
  enabled: undefined as number | undefined
});

const pageData = ref<PageResponse<RoleItem>>({
  records: [],
  current: 1,
  pageSize: 10,
  total: 0,
  totalPages: 0
});

const formModel = reactive<RoleFormPayload>({
  name: '',
  enabled: 1,
  sort: 0,
  deletable: 1,
  remark: '',
  menuIds: []
});

const rules: FormRules<RoleFormPayload> = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 32, message: '角色名称长度需为 2-32 位', trigger: 'blur' }
  ],
  sort: [
    { required: true, message: '请输入排序', trigger: 'change' },
    { type: 'number', min: 0, max: 9999, message: '排序范围为 0-9999', trigger: 'change' }
  ],
  deletable: [{ required: true, message: '请选择允许删除', trigger: 'change' }],
  remark: [{ max: 200, message: '备注不能超过 200 个字符', trigger: 'blur' }]
};

const treeData = computed(() => toTreeSelectData(menuTree.value));
const treeProps = {
  label: 'label',
  children: 'children'
};

const canCreate = computed(() => authStore.hasPermission('sys:role:create'));
const canEdit = computed(() => authStore.hasPermission('sys:role:update'));
const canDelete = computed(() => authStore.hasPermission('sys:role:delete'));

const summary = computed(() => {
  const active = pageData.value.records.filter((item) => item.enabled === 1).length;
  const disabled = pageData.value.records.filter((item) => item.enabled !== 1).length;
  const linkedUsers = pageData.value.records.reduce((sum, item) => sum + (item.userCount || 0), 0);
  return [
    { label: '当前分页角色', value: pageData.value.records.length },
    { label: '启用角色', value: active },
    { label: '停用角色', value: disabled },
    { label: '关联用户', value: linkedUsers }
  ];
});

function resetFormModel() {
  Object.assign(formModel, {
    name: '',
    enabled: 1,
    sort: 0,
    deletable: 1,
    remark: '',
    menuIds: []
  });
}

async function clearDialogState(checkedKeys: number[] = []) {
  await nextTick();
  formRef.value?.clearValidate();
  treeRef.value?.setCheckedKeys(checkedKeys);
}

async function loadData() {
  loading.value = true;
  try {
    pageData.value = await getRoles(query);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载角色失败');
  } finally {
    loading.value = false;
  }
}

async function loadMenus() {
  try {
    menuTree.value = await getMenus();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载菜单失败');
  }
}

async function openCreate() {
  editingId.value = null;
  resetFormModel();
  modalOpen.value = true;
  await clearDialogState();
}

async function openEdit(record: RoleItem) {
  try {
    editingId.value = record.id;
    const detail = await getRoleDetail(record.id);
    Object.assign(formModel, {
      name: detail.name,
      enabled: detail.enabled,
      sort: detail.sort,
      deletable: detail.deletable,
      remark: detail.remark || '',
      menuIds: detail.menuIds || []
    });
    modalOpen.value = true;
    await clearDialogState(detail.menuIds || []);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '加载角色详情失败');
  }
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
  formModel.menuIds = ((treeRef.value?.getCheckedKeys(false) ?? []) as number[]).map((item) => Number(item));
  const payload: RoleFormPayload = {
    ...formModel,
    name: formModel.name.trim(),
    remark: formModel.remark?.trim()
  };
  try {
    if (editingId.value) {
      await updateRole(editingId.value, payload);
      ElMessage.success('角色更新成功');
    } else {
      await createRole(payload);
      ElMessage.success('角色创建成功');
    }
    modalOpen.value = false;
    await loadData();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleDelete(record: RoleItem) {
  try {
    await ElMessageBox.confirm(`确认删除角色 ${record.name}？`, '确认操作', {
      type: 'warning',
      confirmButtonText: '确认',
      cancelButtonText: '取消'
    });
    await deleteRole(record.id);
    ElMessage.success('角色已删除');
    await loadData();
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
  await loadMenus();
  await loadData();
});
</script>

<template>
  <div class="page-stack">
    <section class="section-banner">
      <div>
        <p class="section-kicker">Role Matrix</p>
        <h3>角色管理</h3>
        <p class="section-banner__copy">把权限打包成可分配角色，角色详情页直接承载菜单授权和用户覆盖范围。</p>
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
          <p class="section-kicker">Permission Packs</p>
          <h3>角色目录</h3>
        </div>
        <el-button v-if="canCreate" type="primary" @click="openCreate">新增角色</el-button>
      </div>

      <div class="control-bar">
        <div class="query-bar">
          <el-input v-model="query.keyword" clearable placeholder="角色名搜索" class="query-input" @keyup.enter="handleSearch" />
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
        <el-table-column prop="id" label="角色ID" width="100" />
        <el-table-column prop="name" label="角色名称" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 1 ? 'success' : 'info'" effect="plain">
              {{ row.enabled === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="允许删除" width="120">
          <template #default="{ row }">
            <el-tag :type="row.deletable === 1 ? 'warning' : 'info'" effect="plain">
              {{ row.deletable === 1 ? '允许' : '禁止' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="userCount" label="关联用户" width="110" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button v-if="canEdit" link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button v-if="canDelete && row.deletable === 1" link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="modalOpen" :title="editingId ? '编辑角色' : '新增角色'" width="920px" destroy-on-close>
      <div class="dialog-split">
        <el-form ref="formRef" :model="formModel" :rules="rules" label-position="top">
          <el-form-item label="角色名称" prop="name">
            <el-input v-model="formModel.name" placeholder="例如：运营管理员" />
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="formModel.enabled">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">禁用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="排序" prop="sort">
            <el-input-number v-model="formModel.sort" :min="0" :max="9999" class="full-width" />
          </el-form-item>
          <el-form-item label="允许删除" prop="deletable">
            <el-radio-group v-model="formModel.deletable">
              <el-radio :value="1">允许</el-radio>
              <el-radio :value="0">禁止</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formModel.remark" type="textarea" :rows="4" />
          </el-form-item>
        </el-form>

        <div class="tree-panel">
          <div class="tree-panel__title">菜单与按钮授权</div>
          <div class="tree-panel__body">
            <el-tree
              ref="treeRef"
              node-key="value"
              show-checkbox
              default-expand-all
              :data="treeData"
              :props="treeProps"
            />
          </div>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeDialog">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
