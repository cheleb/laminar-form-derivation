import { defineConfig } from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";

//import { scalaMetadata } from "./scala-metadata"

//const scalaVersion = scalaMetadata.scalaVersion


export default defineConfig({
    plugins: [scalaJSPlugin({
        // path to the directory containing the sbt build
        // default: '.'
        cwd: '../..',

        // sbt project ID from within the sbt build to get fast/fullLinkJS from
        // default: the root project of the sbt build
        projectID: 'client',

        // URI prefix of imports that this plugin catches (without the trailing ':')
        // default: 'scalajs' (so the plugin recognizes URIs starting with 'scalajs:')
        uriPrefix: 'scalajs',
    })],
});


/* https://vitejs.dev/config/
export default defineConfig(({ command, mode, ssrBuild }) => {
    const mainJS = `/target/scala-${scalaVersion}/client-${mode === "production" ? "opt" : "fastopt"
        }/main.js`
    console.log("mainJS", mainJS)
    const script = `<script type="module" src="${mainJS}"></script>`

    return {
        publicDir: "./public",
        plugins: createHtmlPlugin({
            minify: process.env.NODE_ENV === 'production',
            inject: {
                data: {
                    script
                }
            }
        }),
        base: "/laminar-form-derivation/demo",
        server: {
            open: '/laminar-form-derivation/demo'
        }
    }
})
*/

