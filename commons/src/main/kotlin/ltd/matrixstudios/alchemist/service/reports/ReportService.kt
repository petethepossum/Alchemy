package ltd.matrixstudios.alchemist.service.reports

import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.models.report.ReportModel
import ltd.matrixstudios.alchemist.models.profile.GameProfile
import org.bson.Document
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

object ReportService {

    private val handler = Alchemist.dataHandler.createStoreType<UUID, ReportModel>(Alchemist.getDataStoreMethod())
    private val collection = Alchemist.MongoConnectionPool.getCollection("reports")

    private val cache = ConcurrentHashMap<UUID, MutableList<ReportModel>>() // target UUID → reports

    fun save(report: ReportModel) {
        handler.storeAsync(report.id, report)
        recalculateTarget(report.issuedTo)
    }

    fun saveSync(report: ReportModel) {
        handler.store(report.id, report)
        recalculateTarget(report.issuedTo)
    }

    fun getAll(): CompletableFuture<Collection<ReportModel>> {
        return handler.retrieveAllAsync()
    }

    fun getFromCache(target: UUID): Collection<ReportModel> {
        return cache[target] ?: findByTarget(target).get()
    }

    fun recalculateTarget(target: UUID) {
        findByTarget(target).thenApply { cache[target] = it }
    }

    fun recalculatePlayerSync(profile: GameProfile) {
        val reports = findByTarget(profile.uuid).get()
        cache[profile.uuid] = reports
    }

    fun findByTarget(target: UUID): CompletableFuture<MutableList<ReportModel>> {
        return CompletableFuture.supplyAsync {
            val docs = collection.find(Document("target", target.toString()))
            val results = mutableListOf<ReportModel>()

            for (doc in docs) {
                val json = doc.toJson()
                val model = Alchemist.gson.fromJson(json, ReportModel::class.java)
                results.add(model)
            }

            results
        }
    }

    fun findByReporter(reporter: UUID): List<ReportModel> {
        val docs = collection.find(Document("reporter", reporter.toString()))
        return docs.map { doc ->
            Alchemist.gson.fromJson(doc.toJson(), ReportModel::class.java)
        }.toList()
    }

    fun clearOutModels() {
        cache.clear()
    }
}
object ReportIdService {
    private var lastId: Int = 1000 // start from 1000 or load from DB

    fun nextId(): Int {
        lastId += 1
        // optionally persist lastId here
        return lastId
    }
}
