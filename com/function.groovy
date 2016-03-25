
import java.util.List
import java.util.ArrayList;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
class Url
{

def gbk2utf8(gbk)
{
try{
	def l_temp=GBK2Unicode(gbk)
	def l_temp2=unicodeToUtf8(l_temp)
	return l_temp2
}catch(e){e.printStackTrace()}
}
def unicodeToUtf8(theString)
{
	def x=0
	char aChar
	def len=theString.length()
	def outBuffer=new StringBuffer(len)
	while(x<len)
	{
		aChar=theString.charAt(x++)
		if(aChar=='\\')
		{
			aChar=theString.charAt(x++)
			if(aChar=='u')
			{
				def value=0
				def i
				for(i=0;i<4;i++)
				{
					aChar=theString.charAt(x++)
					switch(aChar)
					{
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) +((int)aChar) -((int)'0')
							break
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + ((int)aChar) -((int)'a')
							break
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + ((int)aChar) - ((int)'A')
							break
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.")

					}
				}
				outBuffer.append((char)value)

			}else{
				if(aChar == 't')
				aChar = '\t';
				else if (aChar == 'r')
				aChar = '\r';
				else if (aChar == 'n')
				aChar = '\n';
				else if (aChar == 'f')
				aChar = '\f';
				outBuffer.append(aChar);

			}
		}else
			outBuffer.append(aChar)
		x++
	}
	return outBuffer.toString()
}

def GBK2Unicode(str)
{
	def chr1
	def i
	def result=new StringBuffer()
	for(i=0;i<str.length();i++)
	{
		chr1=(char)str.charAt(i)
		if(!isNeedConvert(chr1))
		{
			result.append(chr1)
			continue
		}
		result.append("\\u"+Integer.toHexString((int)chr1))
	}
	return result.toString()
}
def isNeedConvert(para)
{
	return ((((int)para) & (0x00FF)) !=((int)para))
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



	def Url(String path)
	{
		this.path=path
	}	
	String title
	String path
	String content
}
	def i=1
	def urlList=["http://www.sina.com.cn"]
	def queue= [new Url("http://www.sina.com.cn")]
	def resource="mybatis-config.xml"
	def inputStream=Resources.getResourceAsStream(resource)
	def sqlSessionFactory=new SqlSessionFactoryBuilder().build(inputStream)
	def session=sqlSessionFactory.openSession()
	def mapper=session.getMapper(UrlMapper.class)

	while((!queue.isEmpty())&&i<100000)
	{
		println(queue[0].getPath())
		println(i)
		i++
		try{
		queue[0].content=queue[0].getContent_1(queue[0].path)
		queue[0].title=queue[0].getTitle_1(queue[0].content)[0][2].replace("\n","")
		mapper.insertUrl(queue[0])
                session.commit()
		println(queue[0].title)
		}catch(e){
		e.printStackTrace()
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
				queue.add(new Url(str))
				urlList.add(str)
			}
		}
		queue.remove(0)
	}

interface UrlMapper
{
	void insertUrl(url)
	List<String> selectPathAll()
	Url selectUrlByTitle(pa)
	Url selectUrlByContent(pa)
}
class Para
{
	def search
}
