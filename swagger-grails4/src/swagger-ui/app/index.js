import SwaggerUI from 'swagger-ui'
// or use require, if you prefer
// const SwaggerUI = require('swagger-ui')

SwaggerUI({
    dom_id: '#apiDocRoot',
    url: 'http://localhost:8080/api/doc'
})
