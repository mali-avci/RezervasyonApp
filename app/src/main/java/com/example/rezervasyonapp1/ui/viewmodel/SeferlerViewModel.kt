import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rezervasyonapp1.data.entity.Seferler
import com.example.rezervasyonapp1.data.repo.SeferlerRepository


class SeferlerViewModel : ViewModel() {
    val srepo = SeferlerRepository()
    val seferlerListesi: MutableLiveData<List<Seferler>> = srepo.seferlerListesi
    val anlikSefer = MutableLiveData<Seferler>()


    fun seciliSeferiTakipEt(seferId: String) {
        // Repository'deki yeni canlı dinleme fonksiyonunu çağırıyoruz
        srepo.tekSeferiCanliDinle(seferId) { sefer ->
            anlikSefer.value = sefer // Artık 'sefer' null gelse bile hata vermez
        }
    }
    fun seferleriYukle() {
        srepo.tumSeferleriAl()
    }

    fun ara(kalkis: String, varis: String) {
        srepo.seferAra(kalkis, varis)
    }

    fun kaydet(sefer: Seferler) {
        srepo.seferKaydet(sefer)
    }
    // YENİ: Sefer bilgilerini (Örn: dolu_koltuklar) güncellemek için
    fun guncelle(sefer: Seferler) {
        srepo.seferGuncelle(sefer)
    }

    // YENİ: Sefer silmek için (Admin Paneli için kritik)
    fun sil(seferId: String) {
        srepo.seferSil(seferId)
    }
}