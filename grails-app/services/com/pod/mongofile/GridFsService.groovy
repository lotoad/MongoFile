package com.pod.mongofile

import com.mongodb.Mongo
import com.mongodb.gridfs.GridFS
import grails.transaction.Transactional
import org.springframework.beans.factory.InitializingBean

@Transactional
class GridFsService implements InitializingBean{

    ConfigSlurper mongoSettings
    Mongo mongo
    GridFS gridFS

    //From InitializingBean
    void afterPropertiesSet(){
        this.mongoSettings = grailsApplication.config.mongodb
        mongo = new Mongo(mongoSettings.host.toString(), mongoSettings.port.intValue())
        if (mongoSettings.bucket == null) {
            gridfs = new GridFS(mongo.getDB(mongoSettings.dbName.toString()))
        } else {
            gridfs = new GridFS(mongo.getDB(mongoSettings.dbName.toString()), mongoSettings.bucket.toString())
        }
    }

    /**
     *
     * @param inputStream
     * @param contentType
     * @param filename
     * @return
     */
    def saveFile(inputStream, contentType, filename) {
        def inputFile = gridfs.createFile(inputStream)
        inputFile.setContentType(contentType)
        inputFile.setFilename(filename)
        inputFile.save()
    }

    /**
     *
     * @param filename
     * @return
     */
    def retrieveFile(filename){
        return gridfs.findOne(filename)
    }

    /**
     *
     * @param filename
     * @return
     */
    def deleteFile(String filename) {
        gridfs.remove(filename)
    }

    /**
     *
     * @return
     */
    def getFilesList() {
        def cursor = gridfs.getFileList()
        cursor.toArray()
    }

}
