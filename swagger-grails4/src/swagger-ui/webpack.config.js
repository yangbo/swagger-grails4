const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const outputPath = path.resolve(__dirname, 'dist');

module.exports = {
    entry: './app/index.js',
    output: {
        filename: 'open-api-ui.js',
        path: path.resolve(__dirname, '../../grails-app/assets/javascripts/')
    },
    mode: 'development',
    module: {
        rules: [
            {
                test: /\.yaml$/,
                use: [
                    { loader: 'json-loader' },
                    { loader: 'yaml-loader' }
                ]
            },
            {
                test: /\.css$/,
                use: [
                    { loader: 'style-loader' },
                    { loader: 'css-loader' },
                ]
            }
        ]
    },
    plugins: [
        new CleanWebpackPlugin([
            outputPath
        ]),
        new CopyWebpackPlugin([
            {
                // Copy the Swagger OAuth2 redirect file to the project root;
                // that file handles the OAuth2 redirect after authenticating the end-user.
                from: 'node_modules/swagger-ui/dist/oauth2-redirect.html',
                to: './'
            }
        ]),
        new HtmlWebpackPlugin({
            template: 'index.html'
        })
    ]
};
