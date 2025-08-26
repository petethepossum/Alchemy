package ltd.matrixstudios.alchemist.service.vouchers

import com.mongodb.client.MongoCollection
import ltd.matrixstudios.alchemist.Alchemist
import ltd.matrixstudios.alchemist.models.vouchers.VoucherGrant
import ltd.matrixstudios.alchemist.models.vouchers.VoucherTemplate
import ltd.matrixstudios.alchemist.service.GeneralizedService
import org.bson.Document
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object VoucherService : GeneralizedService {

    val voucherGrants: ConcurrentHashMap<UUID, MutableList<VoucherGrant>> = ConcurrentHashMap()
    val voucherTemplates: ConcurrentHashMap<String, VoucherTemplate> = ConcurrentHashMap()

    val handler = Alchemist.dataHandler.createStoreType<String, VoucherGrant>(Alchemist.getDataStoreMethod())
    val handlerTemplates = Alchemist.dataHandler.createStoreType<String, VoucherTemplate>(Alchemist.getDataStoreMethod())

    val collection: MongoCollection<Document> = Alchemist.MongoConnectionPool.getCollection("vouchergrant")

    fun loadVoucherGrants() {
        handler.retrieveAllAsync().thenAccept { voucherCollection ->
            for (voucher in voucherCollection) {
                val list = voucherGrants.getOrDefault(voucher.givenTo, mutableListOf()).also { it.add(voucher) }
                voucherGrants[voucher.givenTo] = list
            }
        }
    }

    fun loadVoucherTemplates() {
        handlerTemplates.retrieveAllAsync().thenAccept { templates ->
            for (voucher in templates) {
                voucherTemplates[voucher.id] = voucher
            }
        }
    }

    fun findVoucherTemplate(id: String): VoucherTemplate? {
        return voucherTemplates[id] ?: handlerTemplates.retrieve(id)
    }

    fun insertTemplate(voucherTemplate: VoucherTemplate) {
        handlerTemplates.storeAsync(voucherTemplate.id, voucherTemplate)
        voucherTemplates[voucherTemplate.id] = voucherTemplate
    }

    fun insertGrant(target: UUID, newVoucher: VoucherGrant) {
        val list = voucherGrants.getOrDefault(target, mutableListOf()).also { it.add(newVoucher) }
        voucherGrants[target] = list

        // store UUID as string
        handler.storeAsync(target.toString(), newVoucher)
    }

    fun allGrantsFromPlayer(player: UUID): MutableList<VoucherGrant> {
        if (voucherGrants.containsKey(player)) {
            return voucherGrants[player]!!
        }

        val ret = mutableListOf<VoucherGrant>()
        val query = Document("_id", player.toString())
        val items = collection.find(query)

        for (document in items) {
            val gson = Alchemist.gson.fromJson(document.toJson(), VoucherGrant::class.java)
            ret.add(gson)
        }

        return ret
    }
}
