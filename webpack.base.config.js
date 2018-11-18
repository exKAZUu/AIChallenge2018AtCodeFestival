const webpack = require('webpack');

if (!process.env.RUNTIME) {
  throw new Error('Please specify `process.env.RUNTIME`.');
}

module.exports = {
  entry: './src/main.ts',
  resolve: {
    extensions: ['.ts', '.tsx', '.js', '.json'],
  },
  module: {
    rules: [{ test: /\.tsx?$/, loader: 'ts-loader' }],
  },
  plugins: [
    new webpack.DefinePlugin({
      'process.env.RUNTIME': JSON.stringify(process.env.RUNTIME),
    }),
  ],
  devtool: 'inline-source-map',
};
