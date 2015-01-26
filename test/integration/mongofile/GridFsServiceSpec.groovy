package mongofile

import com.pod.mongofile.GridFsService
import grails.test.mixin.TestFor
import spock.lang.Specification


/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(GridFsService)
class GridFsServiceSpec extends Specification {

    def gridFsService

    def setup() {

    }

    def cleanup() {
    }


    void "test getting the most recent file"(){
        when:
            def f = new File("test/resources/SampleScannedDoc.pdf")
            def fis= new FileInputStream(f.getAbsolutePath())
        then:
            def result = gridFsService.saveFile(fis,"application/pdf",f.getName())
            result != null
            println result
        when:
            def gridFsDbFile = gridFsService.retrieveFile(f.name)
        then:
            def date = gridFsDbFile.get("uploadDate")
            date != null
            println date
    }

    void "test listing files"() {
        when:
            def result = gridFsService.getFilesList()
        then:
            result.size() > 0
    }

    void "test adding a file"() {
        given:
        def x = 10
        def f = new File("test/resources/SampleScannedDoc.pdf")
        def fis= new FileInputStream(f.getAbsolutePath())
        def result = gridFsService.saveFile(fis,"application/pdf",f.getName())

        when:
        x++

        then:
        x == 11
    }
}
