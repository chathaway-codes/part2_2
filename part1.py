# Programming languages
# Project 3, Part 1
# 5/5/2014
# Kathleen Tully    tullyk  660461878
# Charles Hathaway  hathac 
import os, re, sys

#traverses directories and files recursively
#base case - does not recurse again if 
#   the current path is not a directory
#   or the current path is already in the dictionary
def traverse_directories(path):
  #is it a directory?
	if os.path.isdir(path):
	  #if so, create 
		sums = {path : {'bytes':0, 'public':0, 'private':0, 'try':0, 'catch':0}}
		for newpath in [os.path.join(path,elem) for elem in os.listdir(path)]:
		  #prevents looping on a link
			if os.path.isdir(newpath) and newpath not in sums:
				#if it's a directory, returns a dictionary
				#also there should be no duplicates due to the recursion
				sums.update(traverse_directories(newpath))
				for x in sums[path]:
					sums[path][x] += sums[newpath][x]
			elif os.path.isfile(newpath) and newpath[-5:].lower() == '.java':
				temp = traverse_directories(newpath)
				sums[path]['bytes'] += temp[0]
				sums[path]['public'] += temp[1]
				sums[path]['private'] += temp[2]
				sums[path]['try'] += temp[3]
				sums[path]['catch'] += temp[4]
		return sums

	elif os.path.isfile(path) and path[-5:].lower() == '.java':
		byts = os.path.getsize(path)
		return (byts,) + read_java(open(path))
# function accepts a filestream of a java file, opens it, 
# counts up stats, closes it and returns counts as a tuple
def read_java(filestream):
	incomment = False
	inquote = False
	public = 0
	private = 0
	tryn = 0
	catch = 0
	for line in filestream:
		#if we're in a comment or quote, check if we can get out of it
		if incomment:
			match = re.search('\*/',line)
			#end the comment/quote and continue reading after comment/quote
			if match:
				incomment = False
				line = line[match.start()+2:]
			#otherwise this whole line is in a comment/quote. just move on
			else: 
				continue
		if inquote:
			match = re.search('(^|[^\\])\"',line)
			#end the comment/quote and continue reading after comment/quote
			if match:
				inquote = False
				line = line[match.start()+1:]
			#otherwise this whole line is in a comment/quote. just move on
			else: 
				continue
		#cut off everything after an inline comment
		line = line.split('//')
		line = line[0]
		#remove block comments that are in one line
		line = re.sub('/\*.*?\*/', ' ', line)
		#remove quotes that are in one line
		line = re.sub('\".*?([^\\\\])\"', ' ', line)
		#remove the beginning if block comment on this line
		line = line.split('/*')
		#check if there is the beginning of a block comment
		if len(line) > 1:
			incomment = True
		#cut off end with comment in it or simply convert the list back to just a string
		line = line[0]
		#now that all the comments have been removed, count up the line stats
		public += len(re.findall('(^|[^a-zA-Z0-9])public($|[^a-zA-Z0-9])',line))
		private += len(re.findall('(^|[^a-zA-Z0-9])private($|[^a-zA-Z0-9])',line))
		tryn += len(re.findall('(^|[^a-zA-Z0-9])try($|[^a-zA-Z0-9])',line))
		catch += len(re.findall('(^|[^a-zA-Z0-9])catch($|[^a-zA-Z0-9])',line))
	filestream.close()
	return public, private, tryn, catch

#starts the recursive traverse_directories() function, the prints the results
def main(path):
	sums = traverse_directories(path)
	#if it's a directory, it will return a dictionary of this format
	if type(sums) is dict:
		for p in sorted(sums):
			print '%s\n\t%d bytes\t%d public\t%d private\t %d try\t %d catch' %(p, sums[p]['bytes'], sums[p]['public'], sums[p]['private'], sums[p]['try'], sums[p]['catch'])
	#if it's a single file, it will return a tuple of this format
	elif type(sums) is tuple:
		print '%s\n\t%d bytes\t%d public\t%d private\t %d try\t %d catch' %(path, sums[0], sums[1], sums[2], sums[3], sums[4])

#checks for proper arguments, then calls main() to begin the main body of the program
if __name__ == '__main__':
	if len(sys.argv) != 2:
		print 'Usage:', sys.argv[0], 'absolute-root-directory'
	#not using isdir to allow for stats on one file
	elif os.path.exists(sys.argv[1]):
		main(sys.argv[1])
	else:
		print "Invalid absolute path name for root directory"