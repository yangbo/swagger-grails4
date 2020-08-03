package swagger.grails4.samples

import swagger.grails4.openapi.ApiDoc

@ApiDoc("Page Command")
trait Page {
    /**
     * Page size
     */
    @ApiDoc("max")
    int max
    /**
     * Offset of records
     */
    @ApiDoc("offset")
    int offset
}
