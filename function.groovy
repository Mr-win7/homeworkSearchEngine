import java.util.List

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
class Url
{
def public static unicodeToUtf8(theString)
{
	
}
def isUTF_8(content)
{
	def regex='<meta .*?charset.*?(utf|UTF)-8[^\\<\\>]*?>'
	return (content=~regex).find()
}
def getChilds(content)
{
	def regex='<a .*? href=[\'"](http://[^"\']*\\.sina\\.com\\.cn[^"\']*).*?>'
	def matcher=content=~regex
	return matcher
}
def getContent_2(url)
{
	
	def content
	def httpclient=HttpClients.createDefault()
	def httpget=new HttpGet(url)
	try {
		def response = httpclient.execute(httpget)
		def entity=response.getEntity()
		if (entity != null)
		{
			def instream = entity.getContent()
			def tmp
			def result=new StringBuffer()
			def reader=new BufferedReader(new InputStreamReader(instream,"ISO-8859-1"))
			while((tmp=reader.readLine())!=null)
			{
				result.append(tmp)
			}
			tmp=result.toString()
			content=new String(tmp.getBytes("ISO-8859-1"),"GB2312")
			instream.close()
		}
		response.close()
	} catch (e) {
		// TODO Auto-generated catch block
		e.printStackTrace()
	}
		
		return content
}
def getContent_1(url)
{
	def i=-1
	def baos=new ByteArrayOutputStream()
	def httpclient=HttpClients.createDefault()
	def httpget=new HttpGet(url)
	try {
		def response = httpclient.execute(httpget)
		def entity=response.getEntity()
		if (entity != null)
		{
			def instream = entity.getContent()
			while((i=instream.read())!=-1)
			{
				baos.write(i)
			}
			instream.close()
		}
		response.close()
	} catch (e) {
		// TODO Auto-generated catch block
		e.printStackTrace()
	}
	def content=baos.toString()
	if(!isUTF_8(content))
	{
		return getContent_2(url)
	}
	else
	{
		return content
	}
}
def getTitle_1(content)
{
	def regex='\\<(title|TITLE)\\>([\\s\\S]*?)\\</(title|TITLE)\\>'
	def matcher=content=~regex
	return matcher
}



	def Url(path)
	{
		this.path=path
	}	
	def title
	def path
	def content
}
	def i=1
	def urlList=["http://www.sina.com.cn"]
	def queue= [new Url("http://www.sina.com.cn")]
	while(!queue.isEmpty())
	{
		println(queue[0].getPath())
		println(i)
		i++
		try{
		queue[0].content=queue[0].getContent_1(queue[0].path)
		queue[0].title=queue[0].getTitle_1(queue[0].content)[0][2].replace("\n","")
		println(queue[0].title)
		}catch(e){
		println("error")
		queue.remove(0)
		continue
		}
		
		def matcher_1=queue[0].getChilds(queue[0].getContent())
		for(ma in matcher_1)
		{
			
			def str=ma[1].replace(" ","").replace("\n","")
			if(!urlList.contains(str))
			{
//				println(str)
				queue.add(new Url(str))
				urlList.add(str)
//				println(i)
//				i++
			}
		}
		queue.remove(0)
	}

interface UrlMapper
{
	def insertUrl(url)
	def selectPathAll()
	def selectUrl(search,pattern)
}
