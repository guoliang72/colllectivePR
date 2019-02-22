#include <stdio.h>
int main()
{
	int a[1000],n,k,i,j,tem;
	scanf("%d",&n);
	for(i=0;i<n;i=i+1)
	{
		scanf("%d",&a[i]);
	}
	scanf("%d",&k);
	for(i=0;i<n-1;i=i+1)
	{
		for(j=0;j<n-i-1;j=j+1)
		{
			if(a[j]<a[j+1])
			{
				tem=a[j];
				a[j]=a[j+1];
				a[j+1]=tem;
			}
		}

	}
	for(i=0;i<k;i=i+1)
	{
		printf("%d\n",&a[i]);		// 输出格式错误
	}
}