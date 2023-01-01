import {defineConfig, PluginOption, UserConfig} from 'vite'

export default defineConfig(({command, mode}): UserConfig => {
    return {
        plugins: command === "build" ? [htmlPlugin()] : [],
        build: {
            target: "esnext"
        },
    };
})

const htmlPlugin = (): PluginOption => {
    return {
        name: "html-transform",
        enforce: 'pre',
        transform: (file, fileName) => {
            return fileName.endsWith('index.html') ? file.replace("-fastopt", "-opt") : file
        }
    };
};
