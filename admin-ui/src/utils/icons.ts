import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import { markRaw, type Component } from 'vue';

type IconOption = {
  label: string;
  value: string;
  component: Component;
};

const iconMap = Object.fromEntries(
  Object.entries(ElementPlusIconsVue).map(([name, component]) => [name, markRaw(component as Component)])
) as Record<string, Component>;

export const menuIconOptions: IconOption[] = Object.entries(iconMap).map(([name, component]) => ({
  label: name,
  value: name,
  component
}));

export function resolveIcon(icon?: string) {
  if (!icon) {
    return iconMap.Menu;
  }
  return iconMap[icon] || iconMap.Menu;
}
