module.exports = {
  publicPath: process.env.NODE_ENV === 'production'
    ? '/demo/'
    : '/',
  devServer: {
	proxy: {
	  '/backend': {
	    target: 'http://localhost:8080',
        changeOrigin: true,
        pathRewrite: {
          '^/backend' : '/'
        }
	  }
	}
  }
}