const ScalaJS = require("./scalajs.webpack.config");
const Merge = require("webpack-merge");

const WebApp = Merge(ScalaJS, {
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                loader: 'ts-loader'
            }
        ]
    },
    resolve: {
        extensions: [ '.ts', '.tsx', '.js' ]
    }
});

module.exports = WebApp;