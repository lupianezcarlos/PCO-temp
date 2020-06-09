import com.sksamuel.scrimage.{Composite, Image}
import com.sksamuel.scrimage.composite.AverageComposite
import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
val conf:Config = ConfigFactory.load();



val uploadPath = "/Users/cl/Desktop/akka-restfull-actor/uploads"
val uploads = new File(uploadPath).listFiles.filter(_.getName.contains("jpg")).sortBy(_.getName)

val seq = Seq(
  "/Users/cl/Desktop/akka-restfull-actor/uploads/5b7fdeab1900001d035028dc.jpg",
  "/Users/cl/Desktop/akka-restfull-actor/uploads/170803_MEDEX_SickDog.jpg.CROP.promo-xlarge2-1.jpg",
  "/Users/cl/Desktop/akka-restfull-actor/uploads/american-eskimo-dog-card-large.jpg",
  "/Users/cl/Desktop/akka-restfull-actor/uploads/GettyImages-521536928-_1_.jpg",
  "/Users/cl/Desktop/akka-restfull-actor/uploads/images.jpg",
  "/Users/cl/Desktop/akka-restfull-actor/uploads/images-1.jpg"
)

val img = Image.fromFile(new File("/Users/cl/Desktop/akka-restfull-actor/uploads/5b7fdeab1900001d035028dc.jpg"))



//def compose(img: Image, overlay: Image) = img.composite(new AverageComposite(0.2), overlay)



def compose(imgs: Seq[String], num: Int): Image = {
  if(num == 0) Image.fromFile(new File(imgs(num)))
  else {
    val alpha = 0.5
    println(alpha)
    compose(imgs, num - 1).composite(new AverageComposite(alpha), Image.fromFile(new File(imgs(num))))
  }
}

compose(seq,seq.size-1).output(new File(uploadPath + "/test.jpg"))


//img6.output(new File(uploadPath + "/test.jpg"))