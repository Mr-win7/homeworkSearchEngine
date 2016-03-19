import java.util.List

import org.apeche.http.*

def getChilds(content)
{
	def regex='<a href=(.*)>'
	def matcher=content=~regex
	matcher.matches()
	return matcher[0]
}
	
class address
{
	def string title
	def string path
	def string content

}


