package com.pod.mongofile

import com.mongodb.DBObject
import com.mongodb.Mongo
import com.mongodb.gridfs.GridFS
import grails.transaction.Transactional
import org.bson.BSONObject
import org.springframework.beans.factory.InitializingBean

@Transactional
class GridFsService implements InitializingBean{

    ConfigObject mongoSettings
    Mongo mongo
    GridFS gridfs
    def grailsApplication

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
        log.info(inputFile)
        inputFile.setContentType(contentType)
        inputFile.setFilename(filename)
        inputFile.save()
        return inputFile.getId()
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
        return gridfs.remove(filename)
    }

    /**
     *
     * @return
     */
    def getFilesList() {
        def cursor = gridfs.getFileList()
        def t = cursor.toArray()
        t.each {
            println it
        }
        return cursor.toArray()
    }

}
