const path = require('path');
const merge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');

const baseConfig = require('../../webpack.base.config');

module.exports = merge(baseConfig, {
  mode: 'production',
  target: 'web',
  output: { libraryTarget: 'var' },
  module: {
    rules: [
      {
        test: /\.scss$/,
        loaders: ['style-loader', 'css-loader', 'sass-loader'],
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, 'src', 'index.html'),
    }),
  ],
  devServer: {
    contentBase: [path.resolve(__dirname, 'dist')],
  },
});
