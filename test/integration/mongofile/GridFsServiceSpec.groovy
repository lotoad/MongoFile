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
    def FILE_NAME = "SampleScannedDoc.pdf"
    def FILE_BASE_PATH = "test/resources/"

    def setup() {
    }

    def cleanup() {
        //Delete everything!
        def fl = gridFsService.retrieveFileList()
        fl.each {
            gridFsService.deleteFile(it.id)
        }
    }


    void "test getting the most recent file"(){
        when:
            def f = new File("${FILE_BASE_PATH}${FILE_NAME}")
            def fis= new FileInputStream(f.getAbsolutePath())
        then:
            def originalFileId = gridFsService.saveFile(fis,"application/pdf",f.getName())
            originalFileId != null
        when:
            println "Original File Id: " + originalFileId
            def newFileId = gridFsService.saveFile(fis,"application/pdf",f.getName())
        then:
            newFileId != null
        when:
            def gridFsDbFile = gridFsService.retrieveFile(FILE_NAME)
        then:
            def files = gridFsDbFile.getAt("uploadDate")
            files != null

            //Ok check for the latest as the returned file.


            gridFsDbFile.id == newFileId //It's getting the old one!
    }

    void "test listing files"() {
        when:
            def f = new File("${FILE_BASE_PATH}${FILE_NAME}")
            def fis= new FileInputStream(f.getAbsolutePath())
            def id = gridFsService.saveFile(fis,"application/pdf",f.getName())
            id = gridFsService.saveFile(fis,"application/pdf",f.getName())
            id = gridFsService.saveFile(fis,"application/pdf",f.getName())
            def result = gridFsService.retrieveFileList()
        then:
            result.size() >= 3
    }

    void "test getting a file by id"(){
        when:
            def f = new File("${FILE_BASE_PATH}${FILE_NAME}")
            def fis= new FileInputStream(f.getAbsolutePath())
            def id, selId = gridFsService.saveFile(fis,"application/pdf",f.getName())
            id = gridFsService.saveFile(fis,"application/pdf",f.getName())
            selId = gridFsService.saveFile(fis,"application/pdf",f.getName())
        then:
            def retrievedFile = gridFsService.retriveFile(selId)
            retrievedFile.id == selId
    }


    void "test getting a particular version of  a file"(){
        when:
            def f = new File("${FILE_BASE_PATH}${FILE_NAME}")
            def fis= new FileInputStream(f.getAbsolutePath())
            def id, versionId
            id = gridFsService.saveFile(fis,"application/pdf",f.getName()) //1
            id = gridFsService.saveFile(fis,"application/pdf",f.getName()) //2
            id = gridFsService.saveFile(fis,"application/pdf",f.getName()) //3
            versionId = gridFsService.saveFile(fis,"application/pdf",f.getName()) //4
            id = gridFsService.saveFile(fis,"application/pdf",f.getName()) //5
            id = gridFsService.saveFile(fis,"application/pdf",f.getName()) //6
        then:
            def gridFsDbFile = gridFsService.retriveFile(versionId)
            def versionGridFsDbFile = gridFsService.retrieveVersion(f.getName(), 4) //Fourth version!
            versionId == versionGridFsDbFile.id
            //gridFsDbFile.id == versionGridFsDbFile.id
    }


    void "test adding a file"() {
        given:
            def x = 10
            def f = new File("${FILE_BASE_PATH}${FILE_NAME}")
            def fis= new FileInputStream(f.getAbsolutePath())
            def result = gridFsService.saveFile(fis,"application/pdf",f.getName())

        when:
            x++

        then:
            x == 11
    }
}
