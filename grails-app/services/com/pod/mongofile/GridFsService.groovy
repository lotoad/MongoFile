package com.pod.mongofile

import com.mongodb.BasicDBObject
import com.mongodb.DB
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.Mongo
import com.mongodb.gridfs.GridFS
import com.mongodb.gridfs.GridFSInputFile
import grails.transaction.Transactional
import org.bson.types.ObjectId
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
        if (mongoSettings.bucket == null || mongoSettings.bucket.size() == 0) {
            gridfs = new GridFS(mongo.getDB(mongoSettings.dbName.toString()))
        } else {
            gridfs = new GridFS(mongo.getDB(mongoSettings.dbName.toString()), mongoSettings.bucket.toString())
        }
    }

    /**
     * Save a file to the Mongo Grid FS store returns the OID of the object.
     * @param inputStream
     * @param contentType
     * @param filename
     * @return
     */
    def saveFile(inputStream, contentType, filename) {
        GridFSInputFile inputFile = gridfs.createFile(inputStream)
        log.info(inputFile)
        inputFile.setContentType(contentType)
        inputFile.setFilename(filename)
        inputFile.save()
        return inputFile.getId()
    }

    /**
     * Retrieves a specific version of the file from 0-N
     * @param filename String
     * @param version Integer
     */
    def retrieveVersion(filename, version){
        def cursor = retrieveFileList(filename)
        def t = cursor.toArray()
        def selected
        t.eachWithIndex { dbFile, i ->
            if(i==version-1){
                selected = dbFile
            }
        }
        return selected
        //throw new Exception("Version not found.")
    }

    /**
     * Retrieve the most recent version of a file.
     * @param filename
     * @return
     */
    def retrieveFile(filename){
        def res = gridfs.find(new BasicDBObject( "filename" , filename )) //Cant sort on this.... unless we use a comparator
        DB db = mongo.getDB(mongoSettings.dbName.toString())
        def fsCol = db.getCollection("fs")
        def collection = fsCol.getCollection("files")
        DBCursor files
        files = collection.find(new BasicDBObject("filename", filename))
        files = files.sort(new BasicDBObject("uploadDate", -1))
        DBObject result = files.first()
        return result
    }

    /**
     *
     * @param gridFsId
     * @return
     */
    def retriveFile(ObjectId gridFsId){
        return gridfs.find(gridFsId)
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
     * @param gridFsId
     * @return
     */
    def deleteFile(ObjectId gridFsId) {
        return gridfs.remove(gridFsId)
    }

    /**
     * GEt a list of files sorted by most oldest first.
     * @return ArrayList of type File
     */
    def retrieveFileList() {
        return retrieveFileList(null)
    }

    /**
     * Get a list of all files and versions for a specific file or all if null passed.
     * @param filename
     * @return
     */
    def retrieveFileList(String filename) {
        def cursor
        if(filename != null) {
            cursor = gridfs.getFileList(new BasicDBObject("filename", filename))
        }else{
            cursor = gridfs.getFileList()
        }
        def t = cursor.sort(new BasicDBObject("uploadDate", 1)).toArray()
        t.each {
            println it
        }
        return cursor.toArray()
    }

}
